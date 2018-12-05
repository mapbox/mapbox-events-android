package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class CertificateBlacklist implements Callback {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private static final String SHA256 = "sha256/";
  private static final long DAY_IN_MILLIS = 86400000;
  private static final String COM_CONFIG_ENDPOINT = "api.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "api.mapbox.cn";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String REQUEST_FAIL = "Request failed to download blacklist";
  private static final String HTTPS_SCHEME = "https";
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String EVENT_CONFIG_SEGMENT = "events-config";
  static final String MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP = "mapboxBlacklistTimestamp";
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.STAGING, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };
  private final Context context;
  private final String accessToken;
  private final String userAgent;
  private final OkHttpClient client;
  private final SharedPreferences sharedPreferences;
  private List<String> revokedKeys;

  CertificateBlacklist(Context context, String accessToken, String userAgent, OkHttpClient client) {
    this.context = context;
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.client = client;
    this.sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    retrieveBlackList();
  }

  List<String> getRevokedKeys() {
    return revokedKeys;
  }

  private void retrieveBlackList() {
    File directory = context.getFilesDir();
    revokedKeys = new ArrayList<>();

    if (directory.isDirectory()) {
      File file = new File(directory, BLACKLIST_FILE);

      if (file.exists()) {
        try {
          revokedKeys = obtainBlacklistContents(file);
        } catch (IOException exception) {
          Log.e(LOG_TAG, exception.getMessage());
        }
      }
    }
  }

  boolean daySinceLastUpdate() {
    long millisecondDiff = System.currentTimeMillis() - retrieveLastUpdateTime();
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  private long retrieveLastUpdateTime() {
    return sharedPreferences.getLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, 0);
  }

  void requestBlacklist(HttpUrl requestUrl) {
    Request request = new Request.Builder()
      .url(requestUrl)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .build();

    client.newCall(request).enqueue(this);
  }

  HttpUrl generateRequestUrl() {
    return new HttpUrl.Builder().scheme(HTTPS_SCHEME)
      .host(determineConfigEndpoint())
      .addPathSegment(EVENT_CONFIG_SEGMENT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
      .build();
  }

  private void saveBlackList(Response response) throws IOException {
    Gson gson = new GsonBuilder().create();
    JsonObject responseJson = gson.fromJson(response.body().string(), JsonObject.class);

    responseJson = addSHAtoKeys(responseJson);
    FileOutputStream outputStream = null;
    saveTimestamp();

    try {
      outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
      outputStream.write(responseJson.toString().getBytes());
    } catch (IOException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    } finally {
      try {
        outputStream.close();
      } catch (IOException exception) {
        Log.e(LOG_TAG, exception.getMessage());
      }
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    Log.e(LOG_TAG, REQUEST_FAIL, exception);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    saveBlackList(response);
  }

  private JsonObject addSHAtoKeys(JsonObject jsonObject) {
    Gson gson = new Gson();

    JsonArray jsonArray = jsonObject.getAsJsonArray("RevokedCertKeys");
    JsonArray shaKeys = new JsonArray();

    for (JsonElement key : jsonArray) {
      shaKeys.add(SHA256 + key.getAsString());
    }

    Type listType = new TypeToken<List<String>>(){}.getType();
    revokedKeys = gson.fromJson(shaKeys.toString(),listType);

    jsonObject.add("RevokedCertKeys", shaKeys);

    return jsonObject;
  }

  private List<String> obtainBlacklistContents(File file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    List<String> blacklist = new ArrayList<>();
    Gson gson = new Gson();

    try {
      JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
      if (jsonObject != null) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("RevokedCertKeys");

        Type listType = new TypeToken<List<String>>(){}.getType();
        blacklist = gson.fromJson(jsonArray.toString(),listType);
      }

    } catch (JsonIOException | JsonSyntaxException exception) {
      Log.e(LOG_TAG, exception.getMessage());
    }

    return blacklist;
  }

  private String determineConfigEndpoint() {
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

  private void saveTimestamp() {
    Date date = new Date();
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, date.getTime());
    editor.apply();
  }
}
