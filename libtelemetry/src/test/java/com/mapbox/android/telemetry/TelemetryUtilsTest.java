package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.junit.Test;


import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelemetryUtilsTest {

  private static final String KEY_META_DATA_INIT_DELAY = "com.mapbox.ComEventsInitDelaySeconds";

  @Test
  public void nonNullMetaDataTest() throws PackageManager.NameNotFoundException {
    Context context = getContext(mock(Bundle.class));

    Bundle metadata = TelemetryUtils.obtainMetaData(context);

    assertTrue(metadata != null);
  }

  @Test
  public void nullMetaDataTest() {
    Context context = mock(Context.class);
    Bundle metadata = TelemetryUtils.obtainMetaData(context);

    assertTrue(metadata == null);
  }

  @Test
  public void defaultDelayNoConfigurationTest() {
    int delay = TelemetryUtils.obtainInitDelay(mock(Context.class));

    assertTrue(delay == 1000);
  }

  @Test
  public void customValidDelayConfigurationTest() throws PackageManager.NameNotFoundException {
    Bundle bundle = getMetadata(2);
    Context context = getContext(bundle);

    int delay = TelemetryUtils.obtainInitDelay(context);

    assertTrue(delay == 2000);
  }

  @Test
  public void customInvalidDelayConfigurationTest() throws PackageManager.NameNotFoundException {
    Bundle bundle = getMetadata(101);
    Context context = getContext(bundle);

    int delay = TelemetryUtils.obtainInitDelay(context);

    assertTrue(delay == 1000);
  }

  private Bundle getMetadata(int delay) {
    Bundle metadata = mock(Bundle.class);
    when(metadata.getInt(eq(KEY_META_DATA_INIT_DELAY))).thenReturn(delay);

    return metadata;
  }

  private Context getContext(Bundle metadata) throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    String packageName = "com.package.test";
    PackageManager packageManager = mock(PackageManager.class);
    ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
    applicationInfo.metaData = metadata;

    when(context.getPackageName()).thenReturn(packageName);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).thenReturn(applicationInfo);

    return context;
  }

}
