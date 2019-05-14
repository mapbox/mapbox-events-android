package com.mapbox.android.telemetry.metrics.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class NetworkUsageInterceptor implements Interceptor {
  private final NetworkUsageMetricsCollector metricsCollector;

  public NetworkUsageInterceptor(NetworkUsageMetricsCollector metricsCollector) {
    this.metricsCollector = metricsCollector;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    RequestBody requestBody = request.body();
    if (requestBody == null) {
      return chain.proceed(request);
    }

    Response response;
    try {
      response = chain.proceed(request);
    } catch (IOException ioe) {
      throw ioe;
    }

    metricsCollector.addTxBytes(requestBody.contentLength());
    ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return response;
    }

    metricsCollector.addRxBytes(responseBody.contentLength());
    return response;
  }
}
