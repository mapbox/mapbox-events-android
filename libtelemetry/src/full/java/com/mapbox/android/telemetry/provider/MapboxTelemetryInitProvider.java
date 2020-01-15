package com.mapbox.android.telemetry.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import com.mapbox.android.core.crashreporter.MapboxUncaughtExceptionHanlder;
import com.mapbox.android.telemetry.BuildConfig;
import com.mapbox.android.telemetry.location.LocationCollectionClient;

import java.util.concurrent.TimeUnit;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static com.mapbox.android.telemetry.location.LocationCollectionClient.DEFAULT_SESSION_ROTATION_INTERVAL_HOURS;

public class MapboxTelemetryInitProvider extends ContentProvider {
  private static final String TAG = "MbxTelemInitProvider";
  private static final String EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY =
    "com.mapbox.android.telemetry.provider.mapboxtelemetryinitprovider";

  @Override
  public boolean onCreate() {
    try {
      if (!BuildConfig.DEBUG) {
        // Install crash reporter for telemetry packages only!
        MapboxUncaughtExceptionHanlder.install(getContext(), MAPBOX_TELEMETRY_PACKAGE, BuildConfig.VERSION_NAME);
      }
      LocationCollectionClient.install(getContext(), TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS));
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
