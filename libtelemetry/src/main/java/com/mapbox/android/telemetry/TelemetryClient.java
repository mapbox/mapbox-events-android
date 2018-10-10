package com.mapbox.android.telemetry;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class TelemetryClient {
  private static final String LOG_TAG = "TelemetryClient";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String EVENTS_ENDPOINT = "/events/v2";
  private static final String ATTACHMENTS_ENDPOINT = "/attachments/v1";
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String EXTRA_DEBUGGING_LOG = "Sending POST to %s with %d event(s) (user agent: %s) "
    + "with payload: %s";
  private static final String BOUNDARY = "--01ead4a5-7a67-4703-ad02-589886e00923";

  private String accessToken;
  private String userAgent;
  private TelemetryClientSettings setting;
  private final Logger logger;
  private CertificateBlacklist certificateBlacklist;
  private MetricUtils metricUtils;
  private int requests;
  private int totalDataTransfer;
  private int cellDataTransfer;
  private int wifiDataTransfer;
  private int appWakeups;
  private int eventCountFailed;
  private int eventCountTotal;
  private int eventCountMax;
  private Location deviceLocation;
  private int deviceTimeDrift;
  private Map<String, Integer> eventCountPerType;
  private Map<String, Integer> failedRequests;

  TelemetryClient(String accessToken, String userAgent, TelemetryClientSettings setting, Logger logger,
                  CertificateBlacklist certificateBlacklist) {
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.setting = setting;
    this.logger = logger;
    this.certificateBlacklist = certificateBlacklist;
    resetCounters();
  }

  void updateAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  void updateUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  void sendEvents(List<Event> events, Callback callback) {
    if (metricUtils.isNewDate()) {
      buildMetricEvent();
    }

    ArrayList<Event> batch = new ArrayList<>();
    batch.addAll(events);
    sendBatch(batch, callback);
    requests++;
    eventCountPerType = metricUtils.calculateEventCountByType(events, eventCountPerType);
  }

  void sendAttachment(Attachment attachment, final CopyOnWriteArraySet<AttachmentListener> attachmentListeners) {
    List<FileAttachment> visionAttachments = attachment.getAttachments();
    List<AttachmentMetadata> metadataList = new ArrayList<>();
    final List<String> fileIds = new ArrayList<>();

    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder(BOUNDARY)
      .setType(MultipartBody.FORM);

    for (FileAttachment fileAttachment: visionAttachments) {
      FileData fileData = fileAttachment.getFileData();
      AttachmentMetadata attachmentMetadata = fileAttachment.getAttachmentMetadata();
      metadataList.add(attachmentMetadata);

      requestBodyBuilder.addFormDataPart("file", attachmentMetadata.getName(),
        RequestBody.create(fileData.getType(), new File(fileData.getFilePath())));

      fileIds.add(attachmentMetadata.getFileId());
    }

    Gson gson = new Gson();
    requestBodyBuilder.addFormDataPart("attachments", gson.toJson(metadataList));

    RequestBody requestBody = reverseMultiForm(requestBodyBuilder);

    HttpUrl baseUrl = setting.getBaseUrl();
    HttpUrl requestUrl = baseUrl.newBuilder(ATTACHMENTS_ENDPOINT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken)
      .build();

    Request request = new Request.Builder()
      .url(requestUrl)
      .post(requestBody)
      .build();

    OkHttpClient client = setting.getAttachmentClient(certificateBlacklist);
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        for (AttachmentListener attachmentListener : attachmentListeners) {
          attachmentListener.onAttachmentFailure(exception.getMessage(), fileIds);
        }
      }

      @Override
      public void onResponse(Call call, Response response) {
        for (AttachmentListener attachmentListener : attachmentListeners) {
          attachmentListener.onAttachmentResponse(response.message(), response.code(), fileIds);
        }
      }
    });
  }

  void updateDebugLoggingEnabled(boolean debugLoggingEnabled) {
    setting = setting.toBuilder().debugLoggingEnabled(debugLoggingEnabled).build();
  }

  // For testing only
  String obtainAccessToken() {
    return accessToken;
  }

  // For testing only
  TelemetryClientSettings obtainSetting() {
    return setting;
  }

  void updateFailedRequests(int code) {
    if (code >= 400) {
      failedRequests = metricUtils.calculateFailedRequests(code, failedRequests);
    }
  }

  private void sendBatch(List<Event> batch, Callback callback) {
    GsonBuilder gsonBuilder = configureGsonBuilder();
    Gson gson = gsonBuilder.create();
    String payload = gson.toJson(batch);
    RequestBody body = RequestBody.create(JSON, payload);
    HttpUrl baseUrl = setting.getBaseUrl();

    HttpUrl url = baseUrl.newBuilder(EVENTS_ENDPOINT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken).build();

    if (isExtraDebuggingNeeded()) {
      logger.debug(LOG_TAG, String.format(Locale.US, EXTRA_DEBUGGING_LOG, url, batch.size(), userAgent, payload));
    }

    Request request = new Request.Builder()
      .url(url)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .post(body)
      .build();

    OkHttpClient client = setting.getClient(certificateBlacklist);
    client.newCall(request).enqueue(callback);
  }

  private boolean isExtraDebuggingNeeded() {
    return setting.isDebugLoggingEnabled() || setting.getEnvironment().equals(Environment.STAGING);
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

  private RequestBody reverseMultiForm(MultipartBody.Builder builder) {
    MultipartBody multipartBody = builder.build();

    builder = new MultipartBody.Builder(BOUNDARY)
      .setType(MultipartBody.FORM);

    for (int i = multipartBody.size() - 1; i > -1 ; i--) {
      builder.addPart(multipartBody.part(i));
    }

    return builder.build();
  }

  private void buildMetricEvent() {
    MetricEvent metricEvent = new MetricEvent();
    metricEvent.setDateUTC(metricUtils.getDateString());
    metricEvent.setRequests(requests);
    metricEvent.setFailedRequests(metricUtils.convertMapToJson(failedRequests));
    metricEvent.setTotalDataTransfer(totalDataTransfer);
    metricEvent.setCellDataTransfer(cellDataTransfer);
    metricEvent.setWifiDataTransfer(wifiDataTransfer);
    metricEvent.setAppWakeups(appWakeups);
    metricEvent.setEventCountPerType(metricUtils.convertMapToJson(eventCountPerType));
    metricEvent.setEventCountFailed(eventCountFailed);
    metricEvent.setEventCountTotal(eventCountTotal);
    metricEvent.setEventCountMax(eventCountMax);
    //    metricEvent.setDeviceLat();
    //    metricEvent.setDeviceLon();
    //    metricEvent.setDeviceTimeDrift();
    //    metricEvent.setConfigResponse();
  }

  private void resetCounters() {
    metricUtils = new MetricUtils();
    requests = 0;
    totalDataTransfer = 0;
    cellDataTransfer = 0;
    wifiDataTransfer = 0;
    appWakeups = 0;
    eventCountFailed = 0;
    eventCountTotal = 0;
    eventCountMax = 0;
  }
}
