package com.mapbox.android.telemetry.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.android.telemetry.location.LocationCollectionClient;

public class MapboxTelemetryInitProvider extends ContentProvider {
  private static final String EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY =
    "com.mapbox.android.telemetry.provider.mapboxtelemetryinitprovider";

  @Override
  public boolean onCreate() {
    // Context is guaranteed to be available at onCreate
    LocationCollectionClient.install(getContext());
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
