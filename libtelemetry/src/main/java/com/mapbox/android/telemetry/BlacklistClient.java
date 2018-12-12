package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class BlacklistClient {
  private static final String LOG_TAG = "BlacklistClient";
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String HTTPS_SCHEME = "https";
  private static final String EVENT_CONFIG_SEGMENT = "events-config";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String COM_CONFIG_ENDPOINT = "api.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "api.mapbox.cn";
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.STAGING, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };
  private final String userAgent;
  private final OkHttpClient client;
  private final Callback callback;

  BlacklistClient(String userAgent, OkHttpClient client, Callback callback) {
    this.userAgent = userAgent;
    this.client = client;
    this.callback = callback;
  }

  void requestBlacklist(HttpUrl requestUrl) {
    Request request = new Request.Builder()
      .url(requestUrl)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .build();

    client.newCall(request).enqueue(callback);
  }

  HttpUrl generateRequestUrl(Context context, String accessToken) {
    return new HttpUrl.Builder().scheme(HTTPS_SCHEME)
      .host(determineConfigEndpoint(context))
      .addPathSegment(EVENT_CONFIG_SEGMENT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
      .build();
  }

  private String determineConfigEndpoint(Context context) {
    EnvironmentChain environmentChain = new EnvironmentChain();
    EnvironmentResolver setupChain = environmentChain.setup();
    ServerInformation serverInformation;
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);

      if (appInformation != null && appInformation.metaData != null) {
        serverInformation = setupChain.obtainServerInformation(appInformation.metaData);
        return ENDPOINTS.get(serverInformation.getEnvironment());
      }
    } catch (PackageManager.NameNotFoundException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    }

    return COM_CONFIG_ENDPOINT;
  }
}
