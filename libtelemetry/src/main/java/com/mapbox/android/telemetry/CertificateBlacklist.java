package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

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
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CertificateBlacklist implements Callback {
  private final long DAY_IN_MILLISECONDS = 86400000;
  private static final String COM_CONFIG_ENDPOINT = "https://config.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "https://config.mapbox.cn";
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };
  private Context context;

  CertificateBlacklist(Context context) {
    this.context = context;
  }

  ArrayList<String> retrieveBlackList() {
    File directory = context.getFilesDir();
    ArrayList<String> blacklist = null;

    if (directory.isDirectory()) {
      File file = new File(directory, "MapboxBlacklist");

      try {
        blacklist = getBlacklistContents(file);
        blacklist.remove(0);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }

    return blacklist;
  }

  boolean daySinceLastUpdate() {
    long millisecondDiff = System.currentTimeMillis() - retriveLastUpdateTime();

    return millisecondDiff >= DAY_IN_MILLISECONDS;
  }

  private long retriveLastUpdateTime() {
    File directory = context.getFilesDir();
    File file = new File(directory, "MapboxBlacklist");

    long lastUpdateTime = 0;
    try {
      ArrayList<String> blacklist = getBlacklistContents(file);
      lastUpdateTime = Long.valueOf(blacklist.get(0));
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    return lastUpdateTime;
  }

  void updateBlacklist() {
    Request request = new Request.Builder()
      .url(determineConfigEndpoint())
      .build();

    OkHttpClient client = new OkHttpClient();
    client.newCall(request).enqueue(this);
  }

  private void saveBlackList(ArrayList<String> revokedKeys) {
    String filename = "MapboxBlacklist";
    String fileContents = createListContent(revokedKeys);
    FileOutputStream outputStream;

    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
      outputStream.write(fileContents.getBytes());
      outputStream.close();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void onFailure(Call call, IOException e) {
    Log.e("CertificateBlacklist", "failure: " + e);
    ArrayList<String> revokedKeys = new ArrayList<>();
    revokedKeys.add("sha256/test1");
    revokedKeys.add("sha256/test2");
    revokedKeys.add("sha256/test3");

    saveBlackList(revokedKeys);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("CertificateBlacklist", "response: " + response);

    //be sure to add sha256/ to returned hashes
    ArrayList<String> revokedKeys = new ArrayList<>();
    revokedKeys.add("sha256/test1");
    revokedKeys.add("sha256/test2");
    revokedKeys.add("sha256/test3");

    saveBlackList(revokedKeys);
  }

  private String createListContent(ArrayList<String> revokedKeys) {
    Date date = new Date();

    String content = "" + date.getTime() + "\n";

    for (String key: revokedKeys) {
      content = content + key + "\n";
    }

    return content;
  }

  private static ArrayList<String> getBlacklistContents(final File file) throws IOException {
    final InputStream inputStream = new FileInputStream(file);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    ArrayList<String> blacklist = new ArrayList<>();

    boolean done = false;

    while (!done) {
      final String line = reader.readLine();
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
      Log.e("CertificateBlacklist", String.valueOf(exception));
    }

    return null;
  }
}
