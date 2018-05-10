package com.mapbox.android.telemetry;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
  private static final int NO_FLAGS = 0;
  private String accessToken;
  private String userAgent;
  private EventsQueue queue;
  private TelemetryClient telemetryClient;
  private TelemetryService telemetryService;
  private Callback httpCallback;
  private final SchedulerFlusher schedulerFlusher;
  private Clock clock = null;
  private ServiceConnection serviceConnection = null;
  private Intent locationServiceIntent = null;
  private final TelemetryEnabler telemetryEnabler;
  private final TelemetryLocationEnabler telemetryLocationEnabler;
  private boolean isLocationOpted = false;
  private boolean isServiceBound = false;
  private PermissionCheckRunnable permissionCheckRunnable = null;
  private CopyOnWriteArraySet<TelemetryListener> telemetryListeners = null;
  static Context applicationContext = null;

  public MapboxTelemetry(Context context, String accessToken, String userAgent) {
    initializeContext(context);
    initializeQueue();
    checkRequiredParameters(accessToken, userAgent);
    this.httpCallback = this;
    AlarmReceiver alarmReceiver = obtainAlarmReceiver();
    this.schedulerFlusher = new SchedulerFlusherFactory(applicationContext, alarmReceiver).supply();
    this.serviceConnection = obtainServiceConnection();
    this.telemetryEnabler = new TelemetryEnabler(true);
    this.telemetryLocationEnabler = new TelemetryLocationEnabler(true);
    initializeTelemetryListeners();
    initializeTelemetryLocationState();
  }

  // For testing only
  MapboxTelemetry(Context context, String accessToken, String userAgent, EventsQueue queue,
                  TelemetryClient telemetryClient, Callback httpCallback, SchedulerFlusher schedulerFlusher,
                  Clock clock, boolean isServiceBound, TelemetryEnabler telemetryEnabler,
                  TelemetryLocationEnabler telemetryLocationEnabler) {
    initializeContext(context);
    this.queue = queue;
    checkRequiredParameters(accessToken, userAgent);
    this.telemetryClient = telemetryClient;
    this.httpCallback = httpCallback;
    this.schedulerFlusher = schedulerFlusher;
    this.clock = clock;
    this.telemetryEnabler = telemetryEnabler;
    this.telemetryLocationEnabler = telemetryLocationEnabler;
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
    flushEnqueuedEvents();
    unregisterTelemetry();
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    for (TelemetryListener telemetryListener : telemetryListeners) {
      telemetryListener.onHttpFailure(exception.getMessage());
    }
  }

  @Override
  public void onResponse(Call call, Response response) {
    response.body().close();
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
    startTelemetryService();
    bindTelemetryService();
    return isLocationOpted;
  }

  boolean optLocationOut() {
    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState();
    if (isServiceBound) {
      telemetryService.unbindInstance();
      telemetryService.removeServiceTask(this);
      if (telemetryService.obtainBoundInstances() == 0
        && TelemetryLocationEnabler.LocationState.ENABLED.equals(telemetryLocationState)) {
        unbindServiceConnection();
        isServiceBound = false;
        stopLocation();
        isLocationOpted = false;
      } else {
        unbindServiceConnection();
        isServiceBound = false;
      }
    }
    return isLocationOpted;
  }

  private void startTelemetryService() {
    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState();
    if (TelemetryLocationEnabler.LocationState.DISABLED.equals(telemetryLocationState) && checkLocationPermission()) {
      startLocation();
      isLocationOpted = true;
    }
  }

  private void bindTelemetryService() {
    applicationContext.bindService(obtainLocationServiceIntent(), serviceConnection, NO_FLAGS);
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
    }
  }

  private TelemetryClient createTelemetryClient(String accessToken, String userAgent) {
    String fullUserAgent = TelemetryUtils.createFullUserAgent(userAgent, applicationContext);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, fullUserAgent,
      new Logger());
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
        telemetryService.addServiceTask(MapboxTelemetry.this);
        if (telemetryService.obtainBoundInstances() == 0) {
          telemetryService.injectEventsQueue(queue);
        }
        telemetryService.bindInstance();
        isServiceBound = true;
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
        telemetryService = null;
        isServiceBound = false;
      }
    };
  }

  private void initializeTelemetryListeners() {
    telemetryListeners = new CopyOnWriteArraySet<>();
  }

  private void initializeTelemetryLocationState() {
    if (!isMyServiceRunning(TelemetryService.class)) {
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.DISABLED);
    }
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  private boolean pushToQueue(Event event) {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      return queue.push(event);
    }
    return false;
  }

  private void unregisterTelemetry() {
    stopAlarm();
    if (isMyServiceRunning(TelemetryService.class)) {
      unbindTelemetryService();
      stopTelemetryService();
    }
  }

  private void stopAlarm() {
    schedulerFlusher.unregister();
  }

  private void unbindTelemetryService() {
    if (isServiceBound) {
      telemetryService.unbindInstance();
      unbindServiceConnection();
    }
  }

  private void stopTelemetryService() {
    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState();
    if (telemetryService.obtainBoundInstances() == 0
      && TelemetryLocationEnabler.LocationState.ENABLED.equals(telemetryLocationState)) {
      stopLocation();
    }
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
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      startAlarm();
      optLocationIn();
      return true;
    }
    return false;
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

  private boolean stopTelemetry() {
    TelemetryEnabler.State telemetryState = telemetryEnabler.obtainTelemetryState();
    if (TelemetryEnabler.State.ENABLED.equals(telemetryState)) {
      flushEnqueuedEvents();
      stopAlarm();
      optLocationOut();
      return true;
    }
    return false;
  }

  private void stopLocation() {
    applicationContext.stopService(obtainLocationServiceIntent());
  }

  private boolean unbindServiceConnection() {
    if (TelemetryUtils.isServiceRunning(TelemetryService.class)) {
      applicationContext.unbindService(serviceConnection);
      return true;
    }

    return false;
  }
}
