package com.mapbox.android.events.testapp;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.telemetry.AppUserTurnstile;
import com.mapbox.android.telemetry.LocationEvent;
import com.mapbox.android.telemetry.MapboxTelemetry;
import com.mapbox.android.telemetry.TelemetryListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener {
  private static final String LOG_TAG = "TelemetryTestApp";
  private static final long DEFAULT_INTERVAL = 2000L;
  private MapboxTelemetry mapboxTelemetry;
  private LocationEngine locationEngine;
  private LocationEngineRequest locationEngineRequest;
  private LocationEngineCallback<LocationEngineResult> locationEngineCallback;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mapboxTelemetry = new MapboxTelemetry(this, obtainAccessToken(), LOG_TAG);
    mapboxTelemetry.updateDebugLoggingEnabled(true);
    mapboxTelemetry.addTelemetryListener(new TelemetryListenerWrapper(this));
    mapboxTelemetry.push(new AppUserTurnstile("fooSdk", "1.0.0"));
    locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
    locationEngineRequest = getRequest(DEFAULT_INTERVAL);
    locationEngineCallback = new LocationCallbackWrapper(this);
    checkPermissions();

    Button disable = findViewById(R.id.disable_telem);
    disable.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mapboxTelemetry.disable();
      }
    });

    Button fillQueue = findViewById(R.id.fill_queue);

    fillQueue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i = 0; i < 180; i++) {
          mapboxTelemetry.push(
            new LocationEvent("testSessionId", 0.0, 0.0, "testAppState"))
          ;
        }
      }
    });
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onResume() {
    super.onResume();
    locationEngine.requestLocationUpdates(locationEngineRequest, locationEngineCallback, null);
  }

  @Override
  public void onPause() {
    super.onPause();
    locationEngine.removeLocationUpdates(locationEngineCallback);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // onDestroy is not consistently called
    // we can't rely on resources clean up here
    mapboxTelemetry.disable();
  }

  private String obtainAccessToken() {
    return getString(R.string.mapbox_access_token);
  }

  @SuppressLint("MissingPermission")
  private void checkPermissions() {
    boolean permissionsGranted = PermissionsManager.areLocationPermissionsGranted(this);
    if (permissionsGranted) {
      mapboxTelemetry.enable();
    } else {
      PermissionsManager permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  private static LocationEngineRequest getRequest(long interval) {
    return new LocationEngineRequest.Builder(interval)
      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
      .build();
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    // noop
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      mapboxTelemetry.enable();
    }
  }

  private static final class LocationCallbackWrapper implements LocationEngineCallback<LocationEngineResult> {
    private final WeakReference<MainActivity> weakReference;

    LocationCallbackWrapper(MainActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
      Location location = result.getLastLocation();
      MainActivity mainActivity = weakReference.get();
      if (location != null && mainActivity != null) {
        Toast.makeText(mainActivity, getLocationText(location), Toast.LENGTH_SHORT).show();
      }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
      MainActivity mainActivity = weakReference.get();
      if (mainActivity != null) {
        Toast.makeText(mainActivity, exception.toString(), Toast.LENGTH_SHORT).show();
      }
    }

    private static String getLocationText(Location location) {
      return location == null ? "Unknown location" :
        location.getProvider() + "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }
  }

  private static final class TelemetryListenerWrapper implements TelemetryListener {
    private final WeakReference<MainActivity> weakReference;

    TelemetryListenerWrapper(MainActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onHttpResponse(boolean successful, int code) {
      final String message = successful ? String.format("Transmission succeed with code: %d", code) :
        String.format("Transmission failed with code: %d", code);
      final MainActivity activity = weakReference.get();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
          }
        });
      }
      Log.i(LOG_TAG, message);
    }

    @Override
    public void onHttpFailure(final String message) {
      final MainActivity activity = weakReference.get();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
          }
        });
      }
      Log.e(LOG_TAG, "Failure: " + message);
    }
  }
}

