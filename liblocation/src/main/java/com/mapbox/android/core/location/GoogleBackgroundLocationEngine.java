package com.mapbox.android.core.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample LocationEngine using Google Play Services
 */
class GoogleBackgroundLocationEngine extends LocationEngine implements
  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final LocationEnginePriority DEFAULT_PRIORITY = LocationEnginePriority.NO_POWER;

  private BroadcastReceiver broadcastReceiver;
  private LocationIntentHandler locationIntentHandler;
  private GoogleApiClient googleApiClient;
  private PendingIntent pendingIntent;
  private String action;
  private Context context;
  private int requestCode;
  private final Map<LocationEnginePriority, UpdateGoogleRequestPriority> REQUEST_PRIORITY = new
    HashMap<LocationEnginePriority, UpdateGoogleRequestPriority>() {
      {
        put(LocationEnginePriority.NO_POWER, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_NO_POWER);
          }
        });
        put(LocationEnginePriority.LOW_POWER, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
          }
        });
        put(LocationEnginePriority.BALANCED_POWER_ACCURACY, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
          }
        });
        put(LocationEnginePriority.HIGH_ACCURACY, new UpdateGoogleRequestPriority() {
          @Override
          public void update(LocationRequest request) {
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
          }
        });
      }
    };

  private GoogleBackgroundLocationEngine(Context context) {
    super();
    googleApiClient = new GoogleApiClient.Builder(context)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
    generateAction();
    generatePendingIntent(context);
    setupBroadcastReceiver();
    this.priority = DEFAULT_PRIORITY;
    this.locationIntentHandler = new LocationIntentHandler();
    this.context = context;
  }

  static synchronized LocationEngine getLocationEngine(Context context) {
    LocationEngine googleBackgroundLocationEngine = new GoogleBackgroundLocationEngine(context.getApplicationContext());

    return googleBackgroundLocationEngine;
  }

  @Override
  public void activate() {
    connect();
  }

  @Override
  public void deactivate() {
    if (googleApiClient != null && googleApiClient.isConnected()) {
      googleApiClient.disconnect();
    }
  }

  @Override
  public boolean isConnected() {
    return googleApiClient.isConnected();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    for (LocationEngineListener listener : locationListeners) {
      listener.onConnected();
    }
  }

  @Override
  public void onConnectionSuspended(int cause) {
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
  }

  @Override
  public Location getLastLocation() {
    if (googleApiClient.isConnected()) {
      //noinspection MissingPermission
      return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    //noinspection MissingPermission
    return null;
  }

  @Override
  public void requestLocationUpdates() {
    registerReceiver();
    LocationRequest request = LocationRequest.create();

    if (interval != null) {
      request.setInterval(interval);
    }
    if (fastestInterval != null) {
      request.setFastestInterval(fastestInterval);
    }
    if (smallestDisplacement != null) {
      request.setSmallestDisplacement(smallestDisplacement);
    }

    updateRequestPriority(request);

    if (googleApiClient.isConnected()) {
      locationIntentHandler.setLocationListeners(locationListeners);
      //noinspection MissingPermission
      LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, pendingIntent);
    }
  }

  @Override
  public void removeLocationUpdates() {
    if (googleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, pendingIntent);
      unregisterReceiver();
    }
  }

  @Override
  public Type obtainType() {
    return Type.GOOGLE_PLAY_SERVICES;
  }

  private void connect() {
    if (googleApiClient != null) {
      if (googleApiClient.isConnected()) {
        onConnected(null);
      } else {
        googleApiClient.connect();
      }
    }
  }

  private void updateRequestPriority(LocationRequest request) {
    REQUEST_PRIORITY.get(priority).update(request);
  }

  private void generatePendingIntent(Context context) {
    requestCode = LocationPendingIntentProvider.generateRequestCode();
    pendingIntent = LocationPendingIntentProvider.buildIntent(context, action, requestCode);
  }

  private void setupBroadcastReceiver() {
    this.broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        locationIntentHandler.handle(intent);
      }
    };
  }

  private void registerReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(action);
    context.registerReceiver(broadcastReceiver, filter);
  }

  private void unregisterReceiver() {
    context.unregisterReceiver(broadcastReceiver);
    LocationPendingIntentProvider.removeRequestCode(requestCode);
  }

  private void generateAction() {
    action = "GoogleLocationEngineBroadcast-" + this.hashCode();
  }
}
