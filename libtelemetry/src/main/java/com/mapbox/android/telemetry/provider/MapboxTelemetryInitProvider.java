package com.mapbox.android.telemetry.provider;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.IBinder;
import android.util.Log;
import com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder;
import com.mapbox.android.telemetry.BuildConfig;
import com.mapbox.android.telemetry.MapboxTelemetryService;
import com.mapbox.android.telemetry.errors.TokenChangeBroadcastReceiver;
import com.mapbox.android.telemetry.location.LocationCollectionClient;

import java.util.concurrent.TimeUnit;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static com.mapbox.android.telemetry.location.LocationCollectionClient.DEFAULT_SESSION_ROTATION_INTERVAL_HOURS;

public class MapboxTelemetryInitProvider extends ContentProvider {
  private static final String TAG = "MbxTelemInitProvider";
  private static final String EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY =
    "com.mapbox.android.telemetry.provider.mapboxtelemetryinitprovider";

  MapboxTelemetryService telemetryService = null;

  private final ServiceConnection telemetryServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      if (service instanceof MapboxTelemetryService.Binder) {
        MapboxTelemetryService.Binder binder = (MapboxTelemetryService.Binder) service;
        telemetryService = binder.getService();
      } else {
        Log.w(TAG, "Invalid type of MapboxTelemetryService.Binder=" + service);
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      telemetryService = null;
    }
  };

  @Override
  public boolean onCreate() {
    try {
      final Context context = getContext();

      if (context == null) {
        Log.e(TAG, "Failed to initialize: context is null");
        return false;
      }

      Intent intent = new Intent(context, MapboxTelemetryService.class);
      context.bindService(intent, telemetryServiceConnection, Context.BIND_AUTO_CREATE);

      if (!BuildConfig.DEBUG) {
        // Register broadcast receiver to get notification
        // when valid token becomes available
        TokenChangeBroadcastReceiver.register(context);

        // Install crash reporter for telemetry packages only!
        MapboxUncaughtExceptionHanlder.install(context, MAPBOX_TELEMETRY_PACKAGE, BuildConfig.VERSION_NAME);
      }
      LocationCollectionClient.install(context, TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS));

      return true;

    } catch (Throwable throwable) {
      // TODO: log silent crash
      Log.e(TAG, throwable.toString());
    }
    return false;
  }

  @Override
  public void attachInfo(Context context, ProviderInfo info) {
    checkContentProviderAuthority(info);
    super.attachInfo(context, info);
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri,
                      @Nullable String[] projection,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs,
                      @Nullable String sortOrder) {
    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,
                    @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values,
                    @Nullable String selection, @Nullable String[] selectionArgs) {
    return 0;
  }

  private static void checkContentProviderAuthority(@NonNull ProviderInfo info) {
    if (info == null) {
      throw new IllegalStateException("MapboxTelemetryInitProvider: ProviderInfo cannot be null.");
    }
    if (EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY.equals(info.authority)) {
      throw new IllegalStateException(
        "Incorrect provider authority in manifest. Most likely due to a missing "
          + "applicationId variable in application's build.gradle.");
    }
  }
}
