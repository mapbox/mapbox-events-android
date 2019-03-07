package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.LOCATION_COLLECTOR_ENABLED;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.SESSION_ROTATION_INTERVAL_MILLIS;

public class MapboxTelemetry implements FullQueueCallback, EventCallback, ServiceTaskCallback {
  private static final String NON_NULL_APPLICATION_CONTEXT_REQUIRED = "Non-null application context required.";
  private static AtomicReference<String> sAccessToken = new AtomicReference<>();
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
    sAccessToken.set(accessToken);
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
    sAccessToken.set(accessToken);
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
      sendEventsIfPossible(fullQueue);
    }
  }

  @Override
  public void onEventReceived(Event event) {
    pushToQueue(event);
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
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(applicationContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(SESSION_ROTATION_INTERVAL_MILLIS,
      TimeUnit.HOURS.toMillis(interval.obtainInterval()));
    editor.apply();
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

  public boolean isQueueEmpty() {
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
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        sendEventsIfPossible(currentEvents);
      }
    });
  }

  private synchronized void sendEventsIfPossible(List<Event> events) {
    if (isNetworkConnected()) {
      sendEvents(events);
    }
  }

  private boolean isNetworkConnected() {
    try {
      ConnectivityManager connectivityManager = (ConnectivityManager)
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      //noinspection MissingPermission
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      if (activeNetwork == null) {
        return false;
      }

      // TODO We should consider using activeNetwork.isConnectedOrConnecting() instead of activeNetwork.isConnected()
      // See ConnectivityReceiver#isConnected(Context context)
      return activeNetwork.isConnected();
    } catch (Exception exception) {
      return false;
    }
  }

  private void sendEvents(List<Event> events) {
    if (checkRequiredParameters(sAccessToken.get(), userAgent)) {
      telemetryClient.sendEvents(events, httpCallback);
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

  private boolean sendEventIfWhitelisted(Event event) {
    if (Event.Type.TURNSTILE.equals(event.obtainType())) {
      final List<Event> appUserTurnstile = new ArrayList<>(1);
      appUserTurnstile.add(event);
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          sendEventsIfPossible(appUserTurnstile);
        }
      });
      return true;
    }
    // Not super concerned about vision, since they most likely doing i/o on bg thread anyways
    if (Event.Type.VIS_ATTACHMENT.equals((event.obtainType()))) {
      sendAttachment(event);
      return true;
    }

    return false;
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

  private static synchronized void enableLocationCollector(boolean enable) {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(applicationContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(LOCATION_COLLECTOR_ENABLED, enable);
    editor.apply();
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
    private ExecutorServiceFactory() {}

    private static synchronized ExecutorService create(String name, int maxSize, long keepAliveSeconds) {
      return new ThreadPoolExecutor(0, maxSize,
        keepAliveSeconds, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
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
}
