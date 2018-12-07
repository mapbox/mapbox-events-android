package com.mapbox.android.telemetry;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class BlacklistClient {
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private final String userAgent;
  private final OkHttpClient client;
  private final Callback callback;

  BlacklistClient(String userAgent, OkHttpClient client, Callback callback) {
    this.userAgent = userAgent;
    this.client = client;
    this.callback = callback;
    Log.e("test", "BlacklistClient created");
  }

  void requestBlacklist(HttpUrl requestUrl) {
    Log.e("test", "requestBlacklist");
    Request request = new Request.Builder()
      .url(requestUrl)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .build();

    client.newCall(request).enqueue(callback);
  }
}
