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
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
  private  final BlacklistClient blacklistClient;
  private List<String> revokedKeys;

  CertificateBlacklist(Context context, String accessToken, String userAgent, OkHttpClient client) {
    this.context = context;
    this.accessToken = accessToken;
    this.blacklistClient = new BlacklistClient(userAgent, client, this);
    retrieveBlackList();
  }

  Boolean isBlacklisted(String hash) {
    return revokedKeys.contains(hash);
  }

  private void retrieveBlackList() {
    File directory = context.getFilesDir();
    revokedKeys = new ArrayList<>();

    if (directory.isDirectory()) {
      File file = new File(directory, BLACKLIST_FILE);

      if (file.exists()) {
        try {
          if (!isFileOpened(file)) {
            revokedKeys = obtainBlacklistContents(file);
          }
        } catch (IOException exception) {
          Log.e(LOG_TAG, exception.getMessage());
        }
      }
    }
  }

  boolean daySinceLastUpdate() {
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    long lastUpdateTime = sharedPreferences.getLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, 0);

    long millisecondDiff = System.currentTimeMillis() - lastUpdateTime;
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  void requestBlacklist(HttpUrl requestUrl) {
    blacklistClient.requestBlacklist(requestUrl);
  }

  HttpUrl generateRequestUrl() {
    return new HttpUrl.Builder().scheme(HTTPS_SCHEME)
      .host(determineConfigEndpoint())
      .addPathSegment(EVENT_CONFIG_SEGMENT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
      .build();
  }

  private void saveBlackList(ResponseBody responseBody) throws IOException {
    Gson gson = new GsonBuilder().create();
    JsonObject responseJson = gson.fromJson(responseBody.string(), JsonObject.class);

    responseJson = addSHAtoKeys(responseJson);
    FileOutputStream outputStream = null;

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

    saveTimestamp();
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    Log.e(LOG_TAG, REQUEST_FAIL, exception);
    saveTimestamp();
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("test", "onResponse: " + response);
    if (response.body() != null) {
      saveBlackList(response.body());
    }
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
    SharedPreferences sharedPreferences = TelemetryUtils.obtainSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putLong(MAPBOX_SHARED_PREFERENCE_KEY_BLACKLIST_TIMESTAMP, System.currentTimeMillis());
    editor.apply();
  }

  private boolean isFileOpened(File file) throws IOException {
    boolean fileOpen = false;
    FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
    FileLock lock = channel.lock();

    try {
      lock = channel.tryLock();
    } catch (OverlappingFileLockException exception) {
      fileOpen = true;
    } catch (IOException exception) {
      exception.printStackTrace();
    } finally {
      lock.release();
    }
    return fileOpen;
  }
}
