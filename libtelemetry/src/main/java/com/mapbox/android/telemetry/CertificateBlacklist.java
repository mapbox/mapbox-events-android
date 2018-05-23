package com.mapbox.android.telemetry;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CertificateBlacklist implements Callback {
  private Context context;

  CertificateBlacklist(Context context) {
    this.context = context;
  }

  ArrayList<String> retrieveBlackList() {

    File directory = context.getFilesDir();
    File file = new File(directory, "MapboxBlacklist");

    ArrayList<String> blacklist = null;
    try {
      blacklist = getBlacklistContents(file);
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    return blacklist;
  }

  void updateBlacklist() {
    Request request = new Request.Builder()
      .url("https://config.mapbox.com")
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
    revokedKeys.add("test1");
    revokedKeys.add("test2");
    revokedKeys.add("test3");

    saveBlackList(revokedKeys);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("CertificateBlacklist", "response: " + response);

    ArrayList<String> revokedKeys = new ArrayList<>();
    revokedKeys.add("test1");
    revokedKeys.add("test2");
    revokedKeys.add("test3");

    saveBlackList(revokedKeys);
  }

  private String createListContent(ArrayList<String> revokedKeys) {
    String content = "";

    for (String key: revokedKeys) {
      content = content + key + "\n";
    }

    return content;
  }

  public static ArrayList<String> getBlacklistContents(final File file) throws IOException {
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
}
