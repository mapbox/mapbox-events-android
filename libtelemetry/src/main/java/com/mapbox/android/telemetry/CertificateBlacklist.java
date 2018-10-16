package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class CertificateBlacklist implements Callback {
  private static final String LOG_TAG = "MapboxBlacklist";
  private static final String BLACKLIST_FILE = "MapboxBlacklist";
  private static final String SHA256 = "sha256/";
  private static final String NEW_LINE = "\n";
  private static final String HTTPS = "https://";
  private static final String BACKSLASH = "/";
  private static final String EMPTY_STRING = "";
  private static final int BLACKLIST_HEAD = 0;
  private static final long DAY_IN_MILLIS = 86400000;
  private static final String COM_CONFIG_ENDPOINT = "api.mapbox.com";
  private static final String CHINA_CONFIG_ENDPOINT = "api.mapbox.cn";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String NAME_NOT_FOUND_EXCEPTION = "Name Not Found";
  private static final String SAVE_BLACKLIST_FAIL = "Unable to save blacklist to file";
  private static final String RETRIEVE_TIME_FAIL = "Unable to retrieve last update time from blacklist";
  private static final String RETRIEVE_BLACKLIST_FAIL = "Unable to retrieve blacklist contents from file";
  private static final String CLOSE_STREAM_FAIL = "Unable to close stream";
  private static final String CLOSE_BUFFER_READER_FAIL = "Unable to close BufferedReader";
  private static final String REQUEST_FAIL = "Request failed to download blacklist";
  private static final String READLINE_FAIL = "Unable to read line of Blacklist file";
  private static final String HTTPS_SCHEME = "https";
  private static final String DATE_FORMAT = "EEE, dd MMM YYYY HH:mm:ss zzz";
  private static final Map<Environment, String> ENDPOINTS = new HashMap<Environment, String>() {
    {
      put(Environment.COM, COM_CONFIG_ENDPOINT);
      put(Environment.STAGING, COM_CONFIG_ENDPOINT);
      put(Environment.CHINA, CHINA_CONFIG_ENDPOINT);
    }
  };
  private final Context context;
  private final String accessToken;
  private Logger logger;

  CertificateBlacklist(Context context, String accessToken) {
    this.context = context;
    this.accessToken = accessToken;
    this.logger = new Logger();
  }

  List<String> retrieveBlackList() {
    File directory = context.getFilesDir();
    List<String> blacklist = new ArrayList<>();

    if (directory.isDirectory()) {
      File file = new File(directory, BLACKLIST_FILE);

      if (file.exists()) {
        try {
          blacklist = obtainBlacklistContents(file);
          blacklist.remove(BLACKLIST_HEAD);
        } catch (IOException exception) {
          logger.error(RETRIEVE_BLACKLIST_FAIL, exception.getMessage());
        }
      }
    }

    return blacklist;
  }

  boolean daySinceLastUpdate() {
    long millisecondDiff = System.currentTimeMillis() - retrieveLastUpdateTime();
    return millisecondDiff >= DAY_IN_MILLIS;
  }

  private long retrieveLastUpdateTime() {
    File directory = context.getFilesDir();
    File file = new File(directory, BLACKLIST_FILE);

    long lastUpdateTime = 0;

    if (file.exists()) {
      try {
        List<String> blacklist = obtainBlacklistContents(file);
        lastUpdateTime = Long.valueOf(blacklist.get(BLACKLIST_HEAD));
      } catch (IOException exception) {
        logger.error(RETRIEVE_TIME_FAIL, exception.getMessage());
      }
    }

    return lastUpdateTime;
  }

  void updateBlacklist() {
    HttpUrl requestUrl = new HttpUrl.Builder().scheme(HTTPS_SCHEME)
      .host(determineConfigEndpoint())
      .addPathSegment("events-config")
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
      .build();

    Request request = new Request.Builder()
      .url(requestUrl)
      .build();

    OkHttpClient client = new OkHttpClient();
    client.newCall(request).enqueue(this);
  }

  private void saveBlackList(List<String> revokedKeys) {
    String fileContents = createListContent(revokedKeys);
    FileOutputStream outputStream = null;

    try {
      outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
      outputStream.write(fileContents.getBytes());
    } catch (IOException exception) {
      logger.error(SAVE_BLACKLIST_FAIL, exception.getMessage());
    } finally {
      try {
        outputStream.close();
      } catch (IOException exception) {
        logger.error(CLOSE_STREAM_FAIL, exception.getMessage());
      }
    }
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    Log.e(LOG_TAG, REQUEST_FAIL, exception);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    String responseData = response.body().string();

    List<String> revokedKeys = extractResponse(responseData);

    setupMetrics(obtainServerTime(response), responseData);
    saveBlackList(revokedKeys);
  }

  private String createListContent(List<String> revokedKeys) {
    Date date = new Date();

    StringBuilder content = new StringBuilder(date.getTime() + NEW_LINE);

    for (String key : revokedKeys) {
      content.append(SHA256).append(key).append(NEW_LINE);
    }

    return content.toString();
  }

  private List<String> obtainBlacklistContents(File file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    List<String> blacklist = new ArrayList<>();

    try {
      boolean done = false;
      while (!done) {
        String line = reader.readLine();
        done = (line == null);

        if (line != null && !line.isEmpty()) {
          blacklist.add(line);
        }
      }

    } catch (IOException exception) {
      logger.error(READLINE_FAIL, exception.getMessage());
    } finally {
      try {
        inputStream.close();
      } catch (IOException exception) {
        logger.error(CLOSE_STREAM_FAIL, exception.getMessage());
      }

      try {
        reader.close();
      } catch (IOException exception) {
        logger.error(CLOSE_BUFFER_READER_FAIL, exception.getMessage());
      }
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
      logger.error(NAME_NOT_FOUND_EXCEPTION, exception.getMessage());
    }

    return COM_CONFIG_ENDPOINT;
  }

  private List<String> extractResponse(String responseData) throws IOException {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);

    Type listType = new TypeToken<List<String>>() {}.getType();
    List revokedKeys = gson.fromJson(jsonObject.get("RevokedCertKeys"), listType);

    return revokedKeys;
  }

  private String[] separateUrlSegments(String url) {
    url = url.replace(HTTPS, EMPTY_STRING);
    return url.split(BACKSLASH);
  }

  private static void setupMetrics(long serverTime, String responseString) {
    MetricUtils.setConfigResponse(responseString);
    MetricUtils.calculateTimeDiff(serverTime);
  }

  private long obtainServerTime(Response response) {
    Headers headers = response.headers();

    String dateString = headers.get("Date");

    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    Date serverDate = new Date();

    try {
      serverDate = dateFormat.parse(dateString);
    } catch (ParseException exception) {
      exception.printStackTrace();
    }

    return serverDate.getTime();
  }
}
