package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.LOCATION_COLLECTOR_ENABLED;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.SESSION_ROTATION_INTERVAL_MILLIS;

public class MapboxTelemetry implements FullQueueCallback, ServiceTaskCallback {
  private static final String LOG_TAG = "MapboxTelemetry";
  private static final String NON_NULL_APPLICATION_CONTEXT_REQUIRED = "Non-null application context required.";
  private static AtomicReference<String> sAccessToken = new AtomicReference<>("");
  private String userAgent;
  private final EventsQueue queue;
  private TelemetryClient telemetryClient;
  private Callback httpCallback;
  private final SchedulerFlusher schedulerFlusher;
  private Clock clock = null;
  private final TelemetryEnabler telemetryEnabler;
  private CopyOnWriteArraySet<TelemetryListener> telemetryListeners = null;
  private CertificateBlacklist certificateBlacklist;
  private CopyOnWriteArraySet<AttachmentListener> attachmentListeners = null;
  private ConfigurationClient configurationClient;
  private final ExecutorService executorService;
  static Context applicationContext = null;

  public MapboxTelemetry(Context context, String accessToken, String userAgent) {
    initializeContext(context);
    setAccessToken(context, accessToken);
    this.userAgent = userAgent;
    AlarmReceiver alarmReceiver = obtainAlarmReceiver();
    this.schedulerFlusher = new SchedulerFlusherFactory(applicationContext, alarmReceiver).supply();
    this.telemetryEnabler = new TelemetryEnabler(true);
    initializeTelemetryListeners();
    initializeAttachmentListeners();
    // Initializing callback after listeners object is instantiated
    this.httpCallback = getHttpCallback(telemetryListeners);
    this.executorService = ExecutorServiceFactory.create("MapboxTelemetryExecutor", 3,
      20);
    this.queue = EventsQueue.create(this, executorService);
  }

  // For testing only
  MapboxTelemetry(Context context, String accessToken, String userAgent, EventsQueue queue,
                  TelemetryClient telemetryClient, Callback httpCallback, SchedulerFlusher schedulerFlusher,
                  Clock clock, TelemetryEnabler telemetryEnabler, ExecutorService executorService) {
    initializeContext(context);
    setAccessToken(context, accessToken);
    this.userAgent = userAgent;
    this.telemetryClient = telemetryClient;
    this.schedulerFlusher = schedulerFlusher;
    this.clock = clock;
    this.telemetryEnabler = telemetryEnabler;
    initializeTelemetryListeners();
    initializeAttachmentListeners();
    this.httpCallback = httpCallback;
    this.executorService = executorService;
    this.queue = queue;
  }

  @Override // Callback is dispatched on background thread
  public void onFullQueue(List<Event> fullQueue) {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)
      && !TelemetryUtils.adjustWakeUpMode(applicationContext)) {
      sendEvents(fullQueue, false);
    }
  }

  @Override
  public void onTaskRemoved() {
    flushEnqueuedEvents();
    unregisterTelemetry();
  }

  public boolean push(Event event) {
    if (sendEventIfWhitelisted(event)) {
      return true;
    }
    // FIXME: push to queue accesses shared preferences
    // TODO: Refactor TelemetryEnabler into async shared prefs change listener
    return pushToQueue(event);
  }

  public boolean enable() {
    if (TelemetryEnabler.isEventsEnabled(applicationContext)) {
      startTelemetry();
      return true;
    }
    return false;
  }

  public boolean disable() {
    if (TelemetryEnabler.isEventsEnabled(applicationContext)) {
      stopTelemetry();
      return true;
    }
    return false;
  }

  public boolean updateSessionIdRotationInterval(SessionInterval interval) {
    final long intervalHours = interval.obtainInterval();
    executeRunnable(new Runnable() {
      @Override
      public void run() {
        try {
          SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(applicationContext);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putLong(SESSION_ROTATION_INTERVAL_MILLIS, TimeUnit.HOURS.toMillis(intervalHours));
          editor.apply();
        } catch (Throwable throwable) {
          // TODO: log silent crash
          Log.e(LOG_TAG, throwable.toString());
        }
      }
    });
    return true;
  }

  public void updateDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
    if (telemetryClient != null) {
      telemetryClient.updateDebugLoggingEnabled(isDebugLoggingEnabled);
    }
  }

  public void updateUserAgent(String userAgent) {
    if (isUserAgentValid(userAgent)) {
      telemetryClient.updateUserAgent(TelemetryUtils.createFullUserAgent(userAgent, applicationContext));
    }
  }

  public boolean updateAccessToken(String accessToken) {
    if (isAccessTokenValid(accessToken) && updateTelemetryClient(accessToken)) {
      sAccessToken.set(accessToken);
      return true;
    }
    return false;
  }

  public boolean addTelemetryListener(TelemetryListener listener) {
    return telemetryListeners.add(listener);
  }

  public boolean removeTelemetryListener(TelemetryListener listener) {
    return telemetryListeners.remove(listener);
  }

  public boolean addAttachmentListener(AttachmentListener listener) {
    return attachmentListeners.add(listener);
  }

  public boolean removeAttachmentListener(AttachmentListener listener) {
    return attachmentListeners.remove(listener);
  }

  boolean isQueueEmpty() {
    return queue.isEmpty();
  }

  // Package private (no modifier) for testing purposes
  boolean checkRequiredParameters(String accessToken, String userAgent) {
    boolean areValidParameters = areRequiredParametersValid(accessToken, userAgent);
    if (areValidParameters) {
      initializeTelemetryClient();
    }
    return areValidParameters;
  }

  private void initializeContext(Context context) {
    if (applicationContext == null) {
      if (context != null && context.getApplicationContext() != null) {
        applicationContext = context.getApplicationContext();
      } else {
        throw new IllegalArgumentException(NON_NULL_APPLICATION_CONTEXT_REQUIRED);
      }
    }
  }

  private boolean areRequiredParametersValid(String accessToken, String userAgent) {
    return isAccessTokenValid(accessToken) && isUserAgentValid(userAgent);
  }

  private boolean isAccessTokenValid(String accessToken) {
    if (!TelemetryUtils.isEmpty(accessToken)) {
      sAccessToken.set(accessToken);
      return true;
    }
    return false;
  }

  private boolean isUserAgentValid(String userAgent) {
    if (!TelemetryUtils.isEmpty(userAgent)) {
      this.userAgent = userAgent;
      return true;
    }
    return false;
  }

  private void initializeTelemetryClient() {
    if (configurationClient == null) {
      this.configurationClient = new ConfigurationClient(applicationContext,
        TelemetryUtils.createFullUserAgent(userAgent, applicationContext), sAccessToken.get(), new OkHttpClient());
    }

    if (certificateBlacklist == null) {
      this.certificateBlacklist = new CertificateBlacklist(applicationContext, configurationClient);
    }

    if (telemetryClient == null) {
      telemetryClient = createTelemetryClient(sAccessToken.get(), userAgent);
    }
  }

  private TelemetryClient createTelemetryClient(String accessToken, String userAgent) {
    String fullUserAgent = TelemetryUtils.createFullUserAgent(userAgent, applicationContext);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, fullUserAgent,
      new Logger(), certificateBlacklist);
    telemetryClient = telemetryClientFactory.obtainTelemetryClient(applicationContext);

    return telemetryClient;
  }

  private boolean updateTelemetryClient(String accessToken) {
    if (telemetryClient != null) {
      telemetryClient.updateAccessToken(accessToken);
      return true;
    }
    return false;
  }

  private AlarmReceiver obtainAlarmReceiver() {
    return new AlarmReceiver(new SchedulerCallback() {
      @Override
      public void onPeriodRaised() {
        flushEnqueuedEvents();
      }

      @Override
      public void onError() {
      }
    });
  }

  private synchronized void flushEnqueuedEvents() {
    final List<Event> currentEvents = queue.flush();
    if (currentEvents.isEmpty()) {
      return;
    }
    executeRunnable(new Runnable() {
      @Override
      public void run() {
        try {
          sendEvents(currentEvents, false);
        } catch (Throwable throwable) {
          // TODO: log silent crash
          Log.e(LOG_TAG, throwable.toString());
        }
      }
    });
  }

  private boolean isNetworkConnected() {
    try {
      ConnectivityManager connectivityManager = (ConnectivityManager)
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      if (activeNetwork == null) {
        return false;
      }
      return activeNetwork.isConnected();
    } catch (Exception exception) {
      return false;
    }
  }

  private synchronized void sendEvents(List<Event> events, boolean serializeNulls) {
    if (isNetworkConnected() && checkRequiredParameters(sAccessToken.get(), userAgent)) {
      telemetryClient.sendEvents(events, httpCallback, serializeNulls);
    }
  }

  private void initializeTelemetryListeners() {
    telemetryListeners = new CopyOnWriteArraySet<>();
  }

  private void initializeAttachmentListeners() {
    attachmentListeners = new CopyOnWriteArraySet<>();
  }

  private boolean pushToQueue(Event event) {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      return queue.push(event);
    }
    return false;
  }

  private void unregisterTelemetry() {
    schedulerFlusher.unregister();
  }

  private synchronized boolean sendEventIfWhitelisted(Event event) {
    boolean isEventSent = false;
    switch (event.obtainType()) {
      case TURNSTILE:
      case CRASH:
        final List<Event> events = Collections.singletonList(event);
        executeRunnable(new Runnable() {
          @Override
          public void run() {
            try {
              sendEvents(events, true);
            } catch (Throwable throwable) {
              // TODO: log silent crash
              Log.e(LOG_TAG, throwable.toString());
            }
          }
        });
        isEventSent = true;
        break;
      case VIS_ATTACHMENT:
        // Not super concerned about vision, since they most likely doing i/o on bg thread anyways
        sendAttachment(event);
        isEventSent = true;
        break;
      default:
        break;
    }
    return isEventSent;
  }

  private void startTelemetry() {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      startAlarm();
      enableLocationCollector(true);
    }
  }

  private void startAlarm() {
    schedulerFlusher.register();
    Clock clock = obtainClock();
    schedulerFlusher.schedule(clock.giveMeTheElapsedRealtime());
  }

  private Clock obtainClock() {
    if (clock == null) {
      clock = new Clock();
    }
    return clock;
  }

  private void stopTelemetry() {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      flushEnqueuedEvents();
      unregisterTelemetry();
      enableLocationCollector(false);
    }
  }

  private synchronized void enableLocationCollector(final boolean enable) {
    executeRunnable(new Runnable() {
      @Override
      public void run() {
        try {
          SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(applicationContext);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putBoolean(LOCATION_COLLECTOR_ENABLED, enable);
          editor.apply();
        } catch (Throwable throwable) {
          // TODO: log silent crash
          Log.e(LOG_TAG, throwable.toString());
        }
      }
    });
  }

  private static synchronized void setAccessToken(@NonNull Context context, @NonNull String accessToken) {
    if (TelemetryUtils.isEmpty(accessToken)) {
      return;
    }
    if (sAccessToken.getAndSet(accessToken).isEmpty()) {
      LocalBroadcastManager.getInstance(context)
        .sendBroadcast(new Intent(MapboxTelemetryConstants.ACTION_TOKEN_CHANGED));
    }
  }

  private void executeRunnable(final Runnable command) {
    try {
      executorService.execute(command);
    } catch (RejectedExecutionException rex) {
      Log.e(LOG_TAG, rex.toString());
    }
  }

  private void sendAttachment(Event event) {
    if (checkNetworkAndParameters()) {
      telemetryClient.sendAttachment(convertEventToAttachment(event), attachmentListeners);
    }
  }

  private Attachment convertEventToAttachment(Event event) {
    return (Attachment) event;
  }

  private Boolean checkNetworkAndParameters() {
    return isNetworkConnected() && checkRequiredParameters(sAccessToken.get(), userAgent);
  }

  private static Callback getHttpCallback(final Set<TelemetryListener> listeners) {
    return new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        for (TelemetryListener telemetryListener : listeners) {
          telemetryListener.onHttpFailure(e.getMessage());
        }
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
          body.close();
        }

        for (TelemetryListener telemetryListener : listeners) {
          telemetryListener.onHttpResponse(response.isSuccessful(), response.code());
        }
      }
    };
  }

  private static final class ExecutorServiceFactory {
    private ExecutorServiceFactory() {
    }

    private static synchronized ExecutorService create(String name, int maxSize, long keepAliveSeconds) {
      return new ThreadPoolExecutor(0, maxSize,
        keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
        threadFactory(name));
    }

    private static ThreadFactory threadFactory(final String name) {
      return new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
          return new Thread(runnable, name);
        }
      };
    }
  }

  @SuppressWarnings("WeakerAccess")
  public synchronized boolean setBaseUrl(String eventsHost) {
    if (isValidUrl(eventsHost) && checkNetworkAndParameters()) {
      telemetryClient.setBaseUrl(eventsHost);
      return true;
    }
    return false;
  }

  private static boolean isValidUrl(String eventsHost) {
    Pattern urlPattern = Pattern.compile("^[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");
    return eventsHost != null && !eventsHost.isEmpty() && urlPattern.matcher(eventsHost).matches();
  }
}
