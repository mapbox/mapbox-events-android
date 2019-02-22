package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.mapbox.libupload.MapboxUploader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UploadClient implements MapboxUploader.MapboxUploadClient, Callback {
  private static final String LOG_TAG = "UploadClient";
  private static final String MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP = "mapboxConfigSyncTimestamp";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String COM_CONFIG_ENDPOINT = "api.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "api.mapbox.cn";
  private static final String EVENTS_ENDPOINT = "/events/v2";
  private static final String HTTPS_SCHEME = "https";
  private static final String EVENT_CONFIG_SEGMENT = "events-config";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final long DAY_IN_MILLIS = 86400000;
  private CertificateBlacklist certificateBlacklist;
  private OkHttpClient client;
  private Context context;
  private String accessToken;
  private String userAgent;
  private TelemetryClientSettings settings;
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.STAGING, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };

  UploadClient(CertificateBlacklist certificateBlacklist, Context context, String accessToken, String userAgent,
               TelemetryClientSettings settings) {
    this.certificateBlacklist = certificateBlacklist;
    this.context = context;
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.settings = settings;

    if (shouldUpdate()) {
      configurationRequest();
    }
  }

  @Override
  public void upload(Object data, Object callback) {
    Log.e(LOG_TAG, "upload: " + data + ", callback: " + callback);

    GsonBuilder gsonBuilder = configureGsonBuilder();
    Gson gson = gsonBuilder.create();
    String payload = gson.toJson(data);
    RequestBody body = RequestBody.create(JSON, payload);
    HttpUrl baseUrl = settings.getBaseUrl();

    HttpUrl url = baseUrl.newBuilder(EVENTS_ENDPOINT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken).build();

//    if (isExtraDebuggingNeeded()) {
//      logger.debug(LOG_TAG, String.format(Locale.US, EXTRA_DEBUGGING_LOG, url, batch.size(), userAgent, payload));
//    }

    Request request = new Request.Builder()
      .url(url)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .post(body)
      .build();

    OkHttpClient client = settings.getClient(certificateBlacklist);
    client.newCall(request).enqueue((Callback) callback);
  }

  boolean shouldUpdate() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    long lastUpdateTime = sharedPreferences.getLong(MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP, 0);
    long millisecondDiff = System.currentTimeMillis() - lastUpdateTime;
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  void configurationRequest() {
    if (client == null) {
      client = new OkHttpClient();
    }

    HttpUrl requestUrl = generateRequestUrl(context, accessToken);
    Request request = new Request.Builder()
      .url(requestUrl)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .build();
    client.newCall(request).enqueue(this);
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

  @Override
  public void onFailure(Call call, IOException e) {
    saveTimestamp();
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

    certificateBlacklist.updateBlacklist(body.string());
  }

  private void saveTimestamp() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(MAPBOX_CONFIG_SYNC_KEY_TIMESTAMP, System.currentTimeMillis());
    editor.apply();
  }

  private GsonBuilder configureGsonBuilder() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    JsonSerializer<NavigationArriveEvent> arriveSerializer = new ArriveEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationArriveEvent.class, arriveSerializer);
    JsonSerializer<NavigationDepartEvent> departSerializer = new DepartEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationDepartEvent.class, departSerializer);
    JsonSerializer<NavigationCancelEvent> cancelSerializer = new CancelEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationCancelEvent.class, cancelSerializer);
    JsonSerializer<NavigationFeedbackEvent> feedbackSerializer = new FeedbackEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationFeedbackEvent.class, feedbackSerializer);
    JsonSerializer<NavigationRerouteEvent> rerouteSerializer = new RerouteEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationRerouteEvent.class, rerouteSerializer);
    JsonSerializer<NavigationFasterRouteEvent> fasterRouteSerializer = new FasterRouteEventSerializer();
    gsonBuilder.registerTypeAdapter(NavigationFasterRouteEvent.class, fasterRouteSerializer);
    return gsonBuilder;
  }
}
