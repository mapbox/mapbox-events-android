package com.mapbox.android.telemetry;


import android.net.TrafficStats;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

final class GzipRequestInterceptor implements Interceptor {
  private static final int THREAD_ID = 10000;

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
      return chain.proceed(originalRequest);
    }

    if (BuildConfig.DEBUG) {
      //Fix strict mode issue, see https://github.com/square/okhttp/issues/3537#issuecomment-431632176
      TrafficStats.setThreadStatsTag(THREAD_ID);
    }

    Request compressedRequest = originalRequest.newBuilder()
      .header("Content-Encoding", "gzip")
      .method(originalRequest.method(), gzip(originalRequest.body()))
      .build();
    return chain.proceed(compressedRequest);
  }

  private RequestBody gzip(final RequestBody body) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return body.contentType();
      }

      @Override
      public long contentLength() {
        return -1; // We don't know the compressed length in advance!
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
        body.writeTo(gzipSink);
        gzipSink.close();
      }
    };
  }
}
