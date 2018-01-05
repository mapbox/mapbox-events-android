package com.mapbox.services.android.telemetry;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.mapbox.services.android.core.location.LocationEnginePriority;
import com.mapbox.services.android.core.permissions.PermissionsManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;

import static com.mapbox.services.android.telemetry.EventReceiver.EVENT_RECEIVER_INTENT;

public class MapboxTelemetry implements FullQueueCallback, EventCallback {
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
  private final Callback httpCallback;
  private final SchedulerFlusher schedulerFlusher;
  private Clock clock = null;
  private LocalBroadcastManager localBroadcastManager = null;
  private Intent locationServiceIntent = null;
  private EventReceiver eventReceiver = null;
  private IntentFilter eventReceiverIntentFilter = null;
  private boolean isTelemetryEnabled = false;
  private boolean isOpted = false;
  private boolean serviceBound = false;
  private PermissionCheckRunnable permissionCheckRunnable = null;
  static Context applicationContext = null;

  public MapboxTelemetry(Context context, String accessToken, String userAgent, Callback httpCallback) {
    initializeContext(context);
    if (checkRequiredParameters(accessToken, userAgent)) {
      initializeTelemetryClient();
    }
    this.httpCallback = httpCallback;
    initializeQueue();
    AlarmReceiver alarmReceiver = obtainAlarmReceiver(httpCallback);
    this.schedulerFlusher = new SchedulerFlusherFactory(applicationContext, alarmReceiver).supply();
  }

  // For testing only
  MapboxTelemetry(Context context, String accessToken, String userAgent, EventsQueue queue,
                  TelemetryClient telemetryClient, Callback httpCallback, SchedulerFlusher schedulerFlusher,
                  Clock clock, LocalBroadcastManager localBroadcastManager) {
    initializeContext(context);
    checkRequiredParameters(accessToken, userAgent);
    this.queue = queue;
    this.telemetryClient = telemetryClient;
    this.httpCallback = httpCallback;
    this.schedulerFlusher = schedulerFlusher;
    this.clock = clock;
    this.localBroadcastManager = localBroadcastManager;
  }

  @Override
  public void onFullQueue(List<Event> fullQueue) {
    sendEventsIfPossible(fullQueue, httpCallback);
  }

  @Override
  public void onEventReceived(Event event) {
    queue.push(event);
  }

  public boolean push(Event event) {
    return queue.push(event);
  }

  public boolean enable() {
    return startTelemetry();
  }

  public boolean disable() {
    return stopTelemetry();
  }

  public boolean optIn() {
    return optLocationIn();
  }

  public boolean optOut() {
    return optLocationOut();
  }

  public void updateSessionIdRotationInterval(int hour) {
    if (serviceBound) {
      SessionIdentifier sessionIdentifier = new SessionIdentifier(hour);
      telemetryService.updateSessionIdentifier(sessionIdentifier);
    }
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

  public void updateLocationPriority(@LocationEnginePriority.PowerMode int locationPriority) {
    if (serviceBound) {
      telemetryService.updateLocationPriority(locationPriority);
    }
  }

  // Package private (no modifier) for testing purposes
  boolean checkRequiredParameters(String accessToken, String userAgent) {
    return isAccessTokenValid(accessToken) && isUserAgentValid(userAgent);
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

  private void initializeContext(Context context) {
    if (applicationContext == null) {
      if (context.getApplicationContext() != null) {
        applicationContext = context.getApplicationContext();
      } else {
        throw new IllegalArgumentException(NON_NULL_APPLICATION_CONTEXT_REQUIRED);
      }
    }
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
    telemetryClient = createTelemetryClient(accessToken, userAgent);
  }

  private TelemetryClient createTelemetryClient(String accessToken, String userAgent) {
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .environment(Environment.STAGING)
      .build();
    telemetryClient = new TelemetryClient(accessToken, userAgent, telemetryClientSettings, new Logger());

    return telemetryClient;
  }

  private void initializeQueue() {
    queue = new EventsQueue(new FullQueueFlusher(this));
    queue.setTelemetryInitialized(true);
  }

  private AlarmReceiver obtainAlarmReceiver(final Callback httpCallback) {
    return new AlarmReceiver(new SchedulerCallback() {
      @Override
      public void onPeriodRaised() {
        // TODO Remove after including UI sample app tests
        System.out.println("MapboxTelemetry#onPeriodRaised");
        List<Event> currentEvents = queue.flush();
        boolean areThereAnyEvents = currentEvents.size() > 0;
        if (areThereAnyEvents) {
          sendEventsIfPossible(currentEvents, httpCallback);
        }
      }

      @Override
      public void onError() {
      }
    });
  }

  private void sendEventsIfPossible(List<Event> events, Callback httpCallback) {
    if (isNetworkConnected()) {
      sendEvents(events, httpCallback);
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

  private void sendEvents(List<Event> events, Callback httpCallback) {
    if (checkRequiredParameters(accessToken, userAgent)) {
      telemetryClient.sendEvents(events, httpCallback);
    }
  }

  private boolean startTelemetry() {
    if (!isTelemetryEnabled) {
      isTelemetryEnabled = true;
      optIn();
      schedulerFlusher.register();
      Clock clock = obtainClock();
      schedulerFlusher.schedule(clock.giveMeTheElapsedRealtime());
    }
    return isTelemetryEnabled;
  }

  private boolean optLocationIn() {
    if (isTelemetryEnabled && !isOpted && checkLocationPermission()) {
      startLocation();
      registerEventReceiver();
      isOpted = true;
    }
    return isOpted;
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

  private Clock obtainClock() {
    if (clock == null) {
      clock = new Clock();
    }

    return clock;
  }

  private boolean stopTelemetry() {
    if (isTelemetryEnabled) {
      optOut();
      schedulerFlusher.unregister();
      isTelemetryEnabled = false;
    }
    return isTelemetryEnabled;
  }

  private boolean optLocationOut() {
    if (isTelemetryEnabled && isOpted) {
      stopLocation();
      unregisterEventReceiver();
      isOpted = false;
    }
    return isOpted;
  }

  private void stopLocation() {
    if (serviceBound) {
      applicationContext.unbindService(serviceConnection);
      serviceBound = false;
    }
    applicationContext.stopService(obtainLocationServiceIntent());
  }

  private void unregisterEventReceiver() {
    LocalBroadcastManager localBroadcastManager = obtainLocalBroadcastManager();
    EventReceiver eventReceiver = obtainEventReceiver();
    localBroadcastManager.unregisterReceiver(eventReceiver);
  }

  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      TelemetryService.TelemetryBinder binder = (TelemetryService.TelemetryBinder) service;
      telemetryService = binder.obtainService();
      serviceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
      telemetryService = null;
      serviceBound = false;
    }
  };
}
