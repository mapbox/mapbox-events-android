package com.mapbox.android.telemetry.crash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.mapbox.android.telemetry.MapboxTelemetryConstants;

/**
 * This broadcast receiver notifies when valid token becomes available.
 * Note: we're not broadcasting users token, we're just interested in getting
 * notified when it becomes available for the first time.
 */
public class TokenChangeBroadcastReceiver extends BroadcastReceiver {

  /**
   * Register receiver with local broadcast manager.
   *
   * @param context valid context.
   */
  public static void register(@NonNull Context context) {
    LocalBroadcastManager.getInstance(context).registerReceiver(new TokenChangeBroadcastReceiver(),
      new IntentFilter(MapboxTelemetryConstants.ACTION_TOKEN_CHANGED));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      // Start background job
      CrashReporterJobIntentService.enqueueWork(context);
      // Unregister receiver - we need it once at startup
      LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    } catch (Throwable throwable) {
      // TODO: log silent crash
    }
  }
}
