package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class ConfigurationClient implements Callback {
  private static final String LOG_TAG = "ConfigurationClient";
  private static final String CONFIG_ERROR_MESSAGE = "Unexpected configuration %s";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String MAPBOX_AGENT_REQUEST_HEADER = "X-Mapbox-Agent";
  private static final String HTTPS_SCHEME = "https";
  private static final String EVENT_CONFIG_SEGMENT = "events-config";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String COM_CONFIG_ENDPOINT = "api.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "api.mapbox.cn";
  private static final String MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP = "mapboxConfigSyncTimestamp";
  private static final long DAY_IN_MILLIS = 86400000;
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.STAGING, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };

  @Nullable
  private final ConfigurationService service;
  private final Context context;
  private final String userAgent;
  private final String reformedUserAgent;
  private final String accessToken;
  private final OkHttpClient client;
  private final List<ConfigurationChangeHandler> handlers;


  ConfigurationClient(Context context,
                      String userAgent,
                      String accessToken,
                      OkHttpClient client,
                      @Nullable ConfigurationService service) {
    this.context = context;
    this.userAgent = userAgent;
    this.accessToken = accessToken;
    this.client = client;
    this.service = service;
    this.handlers = new CopyOnWriteArrayList<>();

    this.reformedUserAgent = TelemetryUtils.createReformedFullUserAgent(context);
  }

  void addHandler(ConfigurationChangeHandler handler) {
    handlers.add(handler);
  }

  boolean shouldUpdate() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    long lastUpdateTime = sharedPreferences.getLong(MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP, 0);
    long millisecondDiff = System.currentTimeMillis() - lastUpdateTime;
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  void update() {
    if (service != null) {
      HttpUrl requestUrl = generateRequestUrl(context, accessToken);
      String payload = "{}";
      RequestBody requestBody = RequestBody.create(JSON, payload);
      Request request = new Request.Builder()
        .url(requestUrl)
        .post(requestBody)
        .header(USER_AGENT_REQUEST_HEADER, userAgent)
        .header(MAPBOX_AGENT_REQUEST_HEADER, reformedUserAgent)
        .build();
      client.newCall(request).enqueue(this);
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    saveTimestamp();
    service.reportError(exception.getMessage());
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    saveTimestamp();
    if (response == null) {
      return;
    }

    ResponseBody body = response.body();
    if (body == null) {
      return;
    }

    try {
      Configuration configuration = new Gson().fromJson(body.string(), Configuration.class);
      if (configuration != null) {
        service.updateConfiguration(configuration);

        for (final ConfigurationChangeHandler handler : handlers) {
          if (handler != null) {
            handler.onUpdate();
          }
        }
      }
    } catch (Exception exception) {
      Log.e(LOG_TAG, exception.toString());
      service.reportError(exception.toString());
    }
  }

  private void saveTimestamp() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP, System.currentTimeMillis());
    editor.apply();
  }

  private static HttpUrl generateRequestUrl(Context context, String accessToken) {
    return new HttpUrl.Builder().scheme(HTTPS_SCHEME)
            .host(determineConfigEndpoint(context))
            .addPathSegment(EVENT_CONFIG_SEGMENT)
            .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
            .build();
  }

  private static String determineConfigEndpoint(Context context) {
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
