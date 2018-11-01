package com.mapbox.android.events.testapp;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.android.telemetry.TelemetryService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MapboxTelemetryActivityTest {
  @Rule
  public GrantPermissionRule permissionRule =
    GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

  @Rule
  public final ServiceTestRule serviceRule = new ServiceTestRule();

  @Test
  public void startServiceBeforeActivity() throws Exception {
    Context context = InstrumentationRegistry.getTargetContext();
    Intent serviceIntent = new Intent(context, TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    serviceRule.startService(serviceIntent);

    Intent intent = new Intent(context, MainActivity.class);
    context.startActivity(intent);

    IBinder binder = serviceRule.bindService(serviceIntent);
    assertTrue(binder.isBinderAlive());
  }

  @Test
  public void startServiceAfterActivityThenKillActivity() throws Exception {
    Context context = InstrumentationRegistry.getTargetContext();
    Intent intent = new Intent(context, MainActivity.class);
    context.startActivity(intent);

    Intent serviceIntent = new Intent(context, TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    serviceRule.startService(serviceIntent);

    IBinder binder = serviceRule.bindService(serviceIntent);
    assertTrue(binder.isBinderAlive());
  }
}
