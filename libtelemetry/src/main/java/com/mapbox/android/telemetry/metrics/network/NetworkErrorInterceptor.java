package com.mapbox.android.telemetry.metrics.network;

import com.mapbox.android.telemetry.metrics.TelemetryMetrics;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

import static com.mapbox.android.telemetry.metrics.TelemetryMetrics.EVENTS_FAILED;
import static com.mapbox.android.telemetry.metrics.TelemetryMetrics.EVENTS_TOTAL;

public class NetworkErrorInterceptor implements Interceptor {
  private final TelemetryMetrics metrics;
  private final int eventCount;

  public NetworkErrorInterceptor(TelemetryMetrics metrics, int eventCount) {
    this.metrics = metrics;
    this.eventCount = eventCount;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());
    metrics.add(EVENTS_TOTAL, eventCount);
    if (!response.isSuccessful()) {
      metrics.add(EVENTS_FAILED, eventCount);
    }
    return response;
  }
}
