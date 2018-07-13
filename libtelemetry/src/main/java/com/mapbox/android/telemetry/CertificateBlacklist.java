package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class CertificateBlacklist implements Callback {
  private static final String LOG_TAG = "MapboxBlacklist";
  private final long DAY_IN_MILLISECONDS = 86400000;
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private static final String SHA256 = "sha256/";
  private static final String NEW_LINE = "\n/";
  private static final int REMOVE_HEAD = 0;
  private static final String REVOKED_CERT_KEYS = "RevokedCertKeys";
  private static final String COM_CONFIG_ENDPOINT = "https://api.mapbox.com/events-config";
  private static final String CHINA_CONFIG_ENDPOINT = "https://api.mapbox.cn/events-config";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };
  private Context context;
  private String accessToken;

  CertificateBlacklist(Context context, String accessToken) {
    this.context = context;
    this.accessToken = accessToken;
  }

  List<String> retrieveBlackList() {
    File directory = context.getFilesDir();
    List<String> blacklist = new ArrayList<>();

    if (directory.isDirectory()) {
      File file = new File(directory, BLACKLIST_FILE);

      if (file.exists()) {
        try {
          blacklist = obtainBlacklistContents(file);
          blacklist.remove(REMOVE_HEAD);
        } catch (IOException exception) {
          Log.e(LOG_TAG, String.valueOf(exception));
        }
      }
    }

    return blacklist;
  }

  boolean daySinceLastUpdate() {
    long millisecondDiff = System.currentTimeMillis() - retrieveLastUpdateTime();

    return millisecondDiff >= DAY_IN_MILLISECONDS;
  }

  private long retrieveLastUpdateTime() {
    File directory = context.getFilesDir();
    File file = new File(directory, BLACKLIST_FILE);

    long lastUpdateTime = 0;

    if (file.exists()) {
      try {
        List<String> blacklist = obtainBlacklistContents(file);
        lastUpdateTime = Long.valueOf(blacklist.get(0));
      } catch (IOException exception) {
        Log.e(LOG_TAG, String.valueOf(exception));
      }
    }

    return lastUpdateTime;
  }

  void updateBlacklist() {
    Request request = new Request.Builder()
      .url(determineConfigEndpoint() + "?" + ACCESS_TOKEN_QUERY_PARAMETER + "=" + accessToken)
      .build();

    OkHttpClient client = new OkHttpClient();
    client.newCall(request).enqueue(this);
  }

  private void saveBlackList(List<String> revokedKeys) {
    String fileContents = createListContent(revokedKeys);
    FileOutputStream outputStream;

    try {
      outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
      outputStream.write(fileContents.getBytes());
      outputStream.close();
    } catch (Exception exception) {
      Log.e(LOG_TAG, String.valueOf(exception));
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    List<String> revokedKeys = extractResponse(response);

    saveBlackList(revokedKeys);
  }

  private String createListContent(List<String> revokedKeys) {
    Date date = new Date();

    StringBuilder content = new StringBuilder(date.getTime() + NEW_LINE);

    for (String key: revokedKeys) {
      content.append(SHA256).append(key).append(NEW_LINE);
    }

    return content.toString();
  }

  private List<String> obtainBlacklistContents(File file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    List<String> blacklist = new ArrayList<>();

    boolean done = false;

    while (!done) {
      String line = reader.readLine();
      done = (line == null);

      if (line != null && !line.isEmpty()) {
        blacklist.add(line);
      }
    }

    reader.close();
    inputStream.close();

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
    } catch (Exception exception) {
      Log.e(LOG_TAG, String.valueOf(exception));
    }

    return null;
  }

  private List<String> extractResponse(Response response) throws IOException {
    String responseData = response.body().string();

    List<String> revokedKeys = new ArrayList<>();

    try {
      JSONObject jsonObject = new JSONObject(responseData);
      JSONArray jsonArray = jsonObject.getJSONArray(REVOKED_CERT_KEYS);

      Gson gson = new Gson();
      TypeToken<List<String>> token = new TypeToken<List<String>>() {};
      revokedKeys = gson.fromJson(String.valueOf(jsonArray), token.getType());

    } catch (JSONException exception) {
      Log.e(LOG_TAG, String.valueOf(exception));
    }

    return revokedKeys;
  }
}
