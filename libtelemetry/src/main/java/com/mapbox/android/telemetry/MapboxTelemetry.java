package com.mapbox.android.telemetry;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.mapbox.android.telemetry.EventReceiver.EVENT_RECEIVER_INTENT;

public class MapboxTelemetry implements FullQueueCallback, EventCallback, ServiceTaskCallback, Callback {
  private static final String EVENTS_USER_AGENT = "MapboxEventsAndroid/";
  private static final String TELEMETRY_USER_AGENT = "MapboxTelemetryAndroid/";
  private static final String UNITY_USER_AGENT = "MapboxEventsUnityAndroid/";
  private static final String NAVIGATION_USER_AGENT = "mapbox-navigation-android/";
  private static final String NAVIGATION_UI_USER_AGENT = "mapbox-navigation-ui-android/";
  private static final List<String> VALID_USER_AGENTS = new ArrayList<String>() {
    {
      add(EVENTS_USER_AGENT);
      add(TELEMETRY_USER_AGENT);
      add(UNITY_USER_AGENT);
      add(NAVIGATION_USER_AGENT);
      add(NAVIGATION_UI_USER_AGENT);
    }
  };
  private static final String NON_NULL_APPLICATION_CONTEXT_REQUIRED = "Non-null application context required.";
  private String accessToken;
  private String userAgent;
  private EventsQueue queue;
  private TelemetryClient telemetryClient;
  private TelemetryService telemetryService;
  private Callback httpCallback;
  private final SchedulerFlusher schedulerFlusher;
  private Clock clock = null;
  private LocalBroadcastManager localBroadcastManager = null;
  private ServiceConnection serviceConnection = null;
  private Intent locationServiceIntent = null;
  private EventReceiver eventReceiver = null;
  private IntentFilter eventReceiverIntentFilter = null;
  private final TelemetryEnabler telemetryEnabler;
  private boolean isOpted = false;
  private boolean isServiceBound = false;
  private PermissionCheckRunnable permissionCheckRunnable = null;
  private CopyOnWriteArraySet<TelemetryListener> telemetryListeners = null;
  static Context applicationContext = null;
  private LocationJobService locationJobService = null;

  public MapboxTelemetry(Context context, String accessToken, String userAgent) {
    initializeContext(context);
    initializeQueue();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      this.locationJobService = new LocationJobService();
    }
    checkRequiredParameters(accessToken, userAgent);
    this.httpCallback = this;
    AlarmReceiver alarmReceiver = obtainAlarmReceiver();
    this.schedulerFlusher = new SchedulerFlusherFactory(applicationContext, alarmReceiver).supply();
    this.serviceConnection = obtainServiceConnection();
    this.telemetryEnabler = new TelemetryEnabler(true);
    initializeTelemetryListeners();
  }

  // For testing only
  MapboxTelemetry(Context context, String accessToken, String userAgent, EventsQueue queue,
                  TelemetryClient telemetryClient, Callback httpCallback, SchedulerFlusher schedulerFlusher,
                  Clock clock, LocalBroadcastManager localBroadcastManager, boolean isServiceBound,
                  TelemetryEnabler telemetryEnabler) {
    initializeContext(context);
    this.queue = queue;
    checkRequiredParameters(accessToken, userAgent);
    this.telemetryClient = telemetryClient;
    this.httpCallback = httpCallback;
    this.schedulerFlusher = schedulerFlusher;
    this.clock = clock;
    this.localBroadcastManager = localBroadcastManager;
    this.telemetryEnabler = telemetryEnabler;
    this.isServiceBound = isServiceBound;
    initializeTelemetryListeners();
  }

  @Override
  public void onFullQueue(List<Event> fullQueue) {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      sendEventsIfPossible(fullQueue);
    }
  }

  @Override
  public void onEventReceived(Event event) {
    pushToQueue(event);
  }

  @Override
  public void onTaskRemoved() {
    if (isServiceBound) {
      stopTelemetry();
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    for (TelemetryListener telemetryListener : telemetryListeners) {
      telemetryListener.onHttpFailure(exception.getMessage());
    }
  }

  @Override
  public void onResponse(Call call, Response response) {
    for (TelemetryListener telemetryListener : telemetryListeners) {
      telemetryListener.onHttpResponse(response.isSuccessful(), response.code());
    }
  }

  public boolean push(Event event) {
    if (sendEventIfWhitelisted(event)) {
      return true;
    }

    boolean isPushed = pushToQueue(event);
    return isPushed;
  }

  public boolean enable() {
    telemetryEnabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);
    startTelemetry();
    startBackgroundLocation();
    return true;
  }

  public boolean disable() {
    stopTelemetry();
    telemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);
    return true;
  }

  public boolean updateSessionIdRotationInterval(SessionInterval interval) {
    if (isServiceBound) {
      int hour = interval.obtainInterval();
      SessionIdentifier sessionIdentifier = new SessionIdentifier(hour);
      telemetryService.updateSessionIdentifier(sessionIdentifier);
      return true;
    }
    return false;
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

  public void updateLocationPriority(LocationEnginePriority locationPriority) {
    if (isServiceBound) {
      telemetryService.updateLocationPriority(locationPriority);
    }
  }

  public boolean updateAccessToken(String accessToken) {
    if (isAccessTokenValid(accessToken) && updateTelemetryClient(accessToken)) {
      this.accessToken = accessToken;
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

  boolean optLocationIn() {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState) && !isOpted && checkLocationPermission()) {
      startLocation();
      registerEventReceiver();
      isOpted = true;
    }
    return isOpted;
  }

  boolean optLocationOut() {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState) && isOpted) {
      stopLocation();
      unregisterEventReceiver();
      isOpted = false;
    }
    return isOpted;
  }

  // Package private (no modifier) for testing purposes
  boolean checkRequiredParameters(String accessToken, String userAgent) {
    boolean areValidParameters = areRequiredParametersValid(accessToken, userAgent);
    if (areValidParameters) {
      initializeTelemetryClient();
      queue.setTelemetryInitialized(true);
    }
    return areValidParameters;
  }

  // Package private (no modifier) for testing purposes
  Intent obtainLocationServiceIntent() {
    if (locationServiceIntent == null) {
      locationServiceIntent = new Intent(applicationContext, TelemetryService.class);
    }

    return locationServiceIntent;
  }

  // Package private (no modifier) for testing purposes
  EventReceiver obtainEventReceiver() {
    if (eventReceiver == null) {
      eventReceiver = new EventReceiver(this);
    }

    return eventReceiver;
  }

  // Package private (no modifier) for testing purposes
  IntentFilter obtainEventReceiverIntentFilter() {
    if (eventReceiverIntentFilter == null) {
      eventReceiverIntentFilter = new IntentFilter(EVENT_RECEIVER_INTENT);
    }

    return eventReceiverIntentFilter;
  }

  // Package private (no modifier) for testing purposes
  void injectTelemetryService(TelemetryService telemetryService) {
    this.telemetryService = telemetryService;
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

  private void initializeQueue() {
    queue = new EventsQueue(new FullQueueFlusher(this));
  }

  private boolean areRequiredParametersValid(String accessToken, String userAgent) {
    return isAccessTokenValid(accessToken) && isUserAgentValid(userAgent);
  }

  private boolean isAccessTokenValid(String accessToken) {
    if (!TelemetryUtils.isEmpty(accessToken)) {
      this.accessToken = accessToken;
      return true;
    }

    return false;
  }

  private boolean isUserAgentValid(String userAgent) {
    if (!TelemetryUtils.isEmpty(userAgent)) {
      for (String userAgentPrefix : VALID_USER_AGENTS) {
        if (userAgent.startsWith(userAgentPrefix)) {
          this.userAgent = userAgent;
          return true;
        }
      }
    }
    return false;
  }

  private void initializeTelemetryClient() {
    if (telemetryClient == null) {
      telemetryClient = createTelemetryClient(accessToken, userAgent);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        locationJobService.setTelemetryClient(telemetryClient);
      }
    }
  }

  private TelemetryClient createTelemetryClient(String accessToken, String userAgent) {
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, userAgent, new Logger());
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
        // TODO Remove after including UI sample app tests
        System.out.println("MapboxTelemetry#onPeriodRaised");
        flushEnqueuedEvents();
      }

      @Override
      public void onError() {
      }
    });
  }

  private void flushEnqueuedEvents() {
    List<Event> currentEvents = queue.flush();
    boolean areThereAnyEvents = currentEvents.size() > 0;
    if (areThereAnyEvents) {
      sendEventsIfPossible(currentEvents);
    }
  }

  private void sendEventsIfPossible(List<Event> events) {
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

      // TODO We should consider using activeNetwork.isConnectedOrConnecting() instead of activeNetwork.isConnected()
      // See ConnectivityReceiver#isConnected(Context context)
      return activeNetwork.isConnected();
    } catch (Exception exception) {
      return false;
    }
  }

  private void sendEvents(List<Event> events) {
    if (checkRequiredParameters(accessToken, userAgent)) {
      telemetryClient.sendEvents(events, httpCallback);
    }
  }

  private ServiceConnection obtainServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {
        TelemetryService.TelemetryBinder binder = (TelemetryService.TelemetryBinder) service;
        telemetryService = binder.obtainService();
        isServiceBound = true;
        telemetryService.injectServiceTask(MapboxTelemetry.this);
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
        telemetryService = null;
        isServiceBound = false;
      }
    };
  }

  private boolean pushToQueue(Event event) {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      return queue.push(event);
    }
    return false;
  }

  private void initializeTelemetryListeners() {
    telemetryListeners = new CopyOnWriteArraySet<>();
  }

  private boolean sendEventIfWhitelisted(Event event) {
    if (Event.Type.TURNSTILE.equals(event.obtainType())) {
      List<Event> appUserTurnstile = new ArrayList<>(1);
      appUserTurnstile.add(event);
      sendEventsIfPossible(appUserTurnstile);
      return true;
    }
    return false;
  }

  private boolean startTelemetry() {
    if (!isOpted) {
      registerFlusher();
    }
    optLocationIn();
    return true;
  }

  private void startBackgroundLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      locationJobService.schedule(applicationContext);
    }
  }

  private boolean checkLocationPermission() {
    if (PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
      return true;
    } else {
      permissionBackoff();
      return false;
    }
  }

  private void permissionBackoff() {
    PermissionCheckRunnable permissionCheckRunnable = obtainPermissionCheckRunnable();
    permissionCheckRunnable.run();
  }

  private PermissionCheckRunnable obtainPermissionCheckRunnable() {
    if (permissionCheckRunnable == null) {
      permissionCheckRunnable = new PermissionCheckRunnable(applicationContext, this);
    }

    return permissionCheckRunnable;
  }

  private void startLocation() {
    applicationContext.startService(obtainLocationServiceIntent());
    applicationContext.bindService(obtainLocationServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private void registerEventReceiver() {
    LocalBroadcastManager localBroadcastManager = obtainLocalBroadcastManager();
    EventReceiver eventReceiver = obtainEventReceiver();
    IntentFilter eventReceiverIntentFilter = obtainEventReceiverIntentFilter();
    localBroadcastManager.registerReceiver(eventReceiver, eventReceiverIntentFilter);
  }

  private LocalBroadcastManager obtainLocalBroadcastManager() {
    if (localBroadcastManager == null) {
      localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
    }

    return localBroadcastManager;
  }

  private void registerFlusher() {
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

  private boolean stopTelemetry() {
    flushEnqueuedEvents();
    if (isOpted) {
      schedulerFlusher.unregister();
    }
    optLocationOut();
    return true;
  }

  private void stopLocation() {
    if (isServiceBound) {
      applicationContext.unbindService(serviceConnection);
      isServiceBound = false;
    }
    applicationContext.stopService(obtainLocationServiceIntent());
  }

  private void unregisterEventReceiver() {
    LocalBroadcastManager localBroadcastManager = obtainLocalBroadcastManager();
    EventReceiver eventReceiver = obtainEventReceiver();
    localBroadcastManager.unregisterReceiver(eventReceiver);
  }
}
