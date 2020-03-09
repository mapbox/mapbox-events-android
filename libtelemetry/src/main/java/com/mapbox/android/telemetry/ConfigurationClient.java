package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.android.core.crashreporter.ErrorReporter;

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

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_CONFIGURATION;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;
import static com.mapbox.android.telemetry.TelemetryEnabler.State;

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

  private final Context context;
  private final String userAgent;
  private final String reformedUserAgent;
  private final String accessToken;
  private final OkHttpClient client;
  private final List<ConfigurationChangeHandler> handlers;
  private final ConfigurationCallback callback;

  ConfigurationClient(Context context,
                      String userAgent,
                      String accessToken,
                      OkHttpClient client,
                      @Nullable ConfigurationCallback callback) {
    this.context = context;
    this.userAgent = userAgent;
    this.accessToken = accessToken;
    this.client = client;
    this.handlers = new CopyOnWriteArrayList<>();
    this.callback = callback;
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
    if (callback != null) {
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
    reportError(exception.getMessage());
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
        persistConfiguration(configuration);

        for (final ConfigurationChangeHandler handler : handlers) {
          if (handler != null) {
            handler.onUpdate();
          }
        }
      }
    } catch (Exception exception) {
      Log.e(LOG_TAG, exception.toString());
      reportError(exception.toString());
    }
  }

  private void persistConfiguration(Configuration configuration) {
    Gson gson = new GsonBuilder().serializeNulls().create();
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(MAPBOX_CONFIGURATION, gson.toJson(configuration));
    editor.apply();

    updateTelemetryState(configuration);
  }

  @VisibleForTesting
  boolean updateTelemetryState(Configuration configuration) {
    boolean updated = false;
    State currentState = new TelemetryEnabler(true).obtainTelemetryState();
    State updatedState = getUpdatedTelemetryState(currentState, configuration);

    if (shouldUpdateTelemetryState(currentState, updatedState)) {
      TelemetryEnabler.updateTelemetryState(updatedState);
      if (currentState == State.CONFIG_DISABLED && (updatedState == State.ENABLED || updatedState == State.OVERRIDE)) {
        callback.enabled();
        updated = true;
      } else if ((currentState == State.ENABLED || currentState == State.OVERRIDE)
        && updatedState == State.CONFIG_DISABLED) {
        callback.disabled();
        updated = true;
      } else {
        Log.d(LOG_TAG, "updateTelemetryState");
      }
    }

    return updated;
  }

  @VisibleForTesting
  boolean shouldUpdateTelemetryState(State currentState, State updatedState) {
    return updatedState != currentState && currentState != State.DISABLED;
  }

  @VisibleForTesting
  State getUpdatedTelemetryState(State currentState, Configuration configuration) {
    State updatedState = currentState;
    Integer type = configuration.getType();
    if (type != null) {
      switch (type) {
        case 0:
          updatedState = State.OVERRIDE;
          break;
        case 1:
          updatedState = State.CONFIG_DISABLED;
          break;
        default:
          reportError(String.format(CONFIG_ERROR_MESSAGE, configuration.toString()));
      }
    } else {
      updatedState = State.ENABLED;
    }

    return updatedState;
  }

  @VisibleForTesting
  void reportError(final String message) {
    ErrorReporter.reportError(context, MAPBOX_TELEMETRY_PACKAGE, new Throwable(message));
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
