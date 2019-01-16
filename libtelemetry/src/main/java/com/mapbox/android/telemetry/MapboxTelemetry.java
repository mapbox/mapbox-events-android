package com.mapbox.android.telemetry;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MapboxTelemetry implements FullQueueCallback, EventCallback, ServiceTaskCallback,
  LifecycleObserver {
  private static final String NON_NULL_APPLICATION_CONTEXT_REQUIRED = "Non-null application context required.";
  private static final String START_SERVICE_FAIL = "Unable to start service";
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
  private final CertificateBlacklist certificateBlacklist;
  private CopyOnWriteArraySet<AttachmentListener> attachmentListeners = null;
  private final ConfigurationClient configurationClient;
  static Context applicationContext = null;

  public MapboxTelemetry(Context context, String accessToken, String userAgent) {
    initializeContext(context);
    initializeQueue();
    this.configurationClient = new ConfigurationClient(context, TelemetryUtils.createFullUserAgent(userAgent,
      context), accessToken, new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    checkRequiredParameters(accessToken, userAgent);
    AlarmReceiver alarmReceiver = obtainAlarmReceiver();
    this.schedulerFlusher = new SchedulerFlusherFactory(applicationContext, alarmReceiver).supply();
    this.serviceConnection = obtainServiceConnection();
    this.telemetryEnabler = new TelemetryEnabler(true);
    this.telemetryLocationEnabler = new TelemetryLocationEnabler(true);
    initializeTelemetryListeners();
    initializeAttachmentListeners();
    initializeTelemetryLocationState(context.getApplicationContext());

    // Initializing callback after listeners object is instantiated
    this.httpCallback = getHttpCallback(telemetryListeners);
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
    initializeAttachmentListeners();
    this.configurationClient = new ConfigurationClient(context, TelemetryUtils.createFullUserAgent(userAgent,
      context), accessToken, new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
  }

  @Override
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

    boolean isPushed = pushToQueue(event);
    return isPushed;
  }

  public boolean enable() {
    if (TelemetryEnabler.isEventsEnabled(applicationContext)) {
      startTelemetry();
      startBackgroundLocation();
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
    if (isServiceBound && telemetryService != null) {
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

  public boolean addAttachmentListener(AttachmentListener listener) {
    return attachmentListeners.add(listener);
  }

  public boolean removeAttachmentListener(AttachmentListener listener) {
    return attachmentListeners.remove(listener);
  }

  boolean optLocationIn() {
    startTelemetryService();
    bindTelemetryService();
    return isLocationOpted;
  }

  boolean optLocationOut() {
    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState(applicationContext);
    if (isServiceBound && telemetryService != null) {
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

  boolean isQueueEmpty() {
    return queue.queue.size() == 0;
  }

  private void startTelemetryService() {
    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState(applicationContext);
    if (TelemetryLocationEnabler.LocationState.DISABLED.equals(telemetryLocationState) && checkLocationPermission()) {
      startLocation(isLollipopOrHigher());
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
      this.userAgent = userAgent;
      return true;
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
    if (checkRequiredParameters(accessToken, userAgent)) {
      telemetryClient.sendEvents(events, httpCallback);
    }
  }

  private ServiceConnection obtainServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {
        if (service instanceof TelemetryService.TelemetryBinder) {
          TelemetryService.TelemetryBinder binder = (TelemetryService.TelemetryBinder) service;
          telemetryService = binder.obtainService();
          telemetryService.addServiceTask(MapboxTelemetry.this);
          if (telemetryService.obtainBoundInstances() == 0) {
            telemetryService.injectEventsQueue(queue);
          }
          telemetryService.bindInstance();
          isServiceBound = true;
        } else {
          applicationContext.stopService(obtainLocationServiceIntent());
        }
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

  private void initializeAttachmentListeners() {
    attachmentListeners = new CopyOnWriteArraySet<>();
  }

  private void initializeTelemetryLocationState(Context context) {
    if (!isMyServiceRunning(TelemetryService.class)) {
      telemetryLocationEnabler.updateTelemetryLocationState(TelemetryLocationEnabler.LocationState.DISABLED, context);
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
    if (isServiceBound && telemetryService != null) {
      telemetryService.unbindInstance();
      unbindServiceConnection();
    }
  }

  private void stopTelemetryService() {
    if (telemetryService == null) {
      return;
    }

    TelemetryLocationEnabler.LocationState telemetryLocationState = telemetryLocationEnabler
      .obtainTelemetryLocationState(applicationContext);
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

    if (Event.Type.VIS_ATTACHMENT.equals((event.obtainType()))) {
      sendAttachment(event);
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

  @SuppressLint("MissingPermission")
  private void startBackgroundLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      final LocationManager locationManager = (LocationManager) applicationContext
        .getSystemService(Context.LOCATION_SERVICE);

      LocationListener locationListener = new LocationListener() {
        @SuppressLint("NewApi")
        @Override
        public void onLocationChanged(Location location) {
          locationManager.removeUpdates(this);

          LocationJobService.schedule(applicationContext, userAgent, accessToken, location);
          startIntentLocationService();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
      };

      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, locationListener);
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

  @VisibleForTesting
  void startLocation(boolean isLollipopOrHigher) {
    if (isLollipopOrHigher) {
      if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        return;
      }
    }

    try {
      applicationContext.startService(obtainLocationServiceIntent());
    } catch (IllegalStateException exception) {
      Log.e(START_SERVICE_FAIL, exception.getMessage());
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
    if (TelemetryUtils.isServiceRunning(TelemetryService.class, applicationContext)) {
      applicationContext.unbindService(serviceConnection);
      return true;
    }

    return false;
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
    return isNetworkConnected() && checkRequiredParameters(accessToken, userAgent);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  void onEnterForeground() {
    startLocation(isLollipopOrHigher());
    ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
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

  private boolean isLollipopOrHigher() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  //test
  @SuppressLint("MissingPermission")
  private void startIntentLocationService() {
    LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(1000L)
      .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
      .setMaxWaitTime(1000L * 5).build();

    LocationIntentService pendingIntent = new LocationIntentService();

    FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext);
    fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(locationEngineRequest), pendingIntent.getPendingIntent())
      .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          Log.e("test", "Location updates are enabled");
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.e("test","Enabling location updates failed");
        }
      });
  }

  private static LocationRequest toGMSLocationRequest(LocationEngineRequest request) {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(request.getInterval());
    locationRequest.setFastestInterval(request.getFastestInterval());
    locationRequest.setSmallestDisplacement(request.getDisplacemnt());
    locationRequest.setMaxWaitTime(request.getMaxWaitTime());
    locationRequest.setPriority(toGMSLocationPriority(request.getPriority()));
    return locationRequest;
  }

  private static int toGMSLocationPriority(int enginePriority) {
    switch (enginePriority) {
      case LocationEngineRequest.PRIORITY_HIGH_ACCURACY:
        return LocationRequest.PRIORITY_HIGH_ACCURACY;
      case LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
      case LocationEngineRequest.PRIORITY_LOW_POWER:
        return LocationRequest.PRIORITY_LOW_POWER;
      case LocationEngineRequest.PRIORITY_NO_POWER:
      default:
        return LocationRequest.PRIORITY_NO_POWER;
    }
  }
}
