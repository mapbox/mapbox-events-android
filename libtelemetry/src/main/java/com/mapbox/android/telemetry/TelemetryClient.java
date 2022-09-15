package com.mapbox.android.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
  private static final String MAPBOX_AGENT_REQUEST_HEADER = "X-Mapbox-Agent";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";
  private static final String EXTRA_DEBUGGING_LOG = "Sending POST to %s with %d event(s) (user agent: %s) "
    + "with payload: %s";
  private static final String BOUNDARY = "--01ead4a5-7a67-4703-ad02-589886e00923";

  private String accessToken;
  private String userAgent;
  private String reformedUserAgent;
  private TelemetryClientSettings setting;
  private final Logger logger;
  private CertificateBlacklist certificateBlacklist;
  private boolean isCnRegion;

  TelemetryClient(String accessToken, String userAgent, String reformedUserAgent, TelemetryClientSettings setting,
                  Logger logger, CertificateBlacklist certificateBlacklist, boolean isCnRegion) {
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.reformedUserAgent = reformedUserAgent;
    this.setting = setting;
    this.logger = logger;
    this.certificateBlacklist = certificateBlacklist;
    this.isCnRegion = isCnRegion;
  }

  boolean isCnRegion() {
    return this.isCnRegion;
  }

  void updateAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  void updateUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  void sendEvents(List<Event> events, Callback callback, boolean serializeNulls) {
    sendBatch(Collections.unmodifiableList(events), callback, serializeNulls);
  }

  void sendAttachment(Attachment attachment, final CopyOnWriteArraySet<AttachmentListener> attachmentListeners) {
    List<FileAttachment> visionAttachments = attachment.getAttachments();
    List<AttachmentMetadata> metadataList = new ArrayList<>();
    final List<String> fileIds = new ArrayList<>();

    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder(BOUNDARY)
      .setType(MultipartBody.FORM);

    for (FileAttachment fileAttachment : visionAttachments) {
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

    if (isExtraDebuggingNeeded()) {
      logger.debug(LOG_TAG, String.format(Locale.US, EXTRA_DEBUGGING_LOG, requestUrl, visionAttachments.size(),
        userAgent, metadataList));
    }

    Request request = new Request.Builder()
      .url(requestUrl)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .addHeader(MAPBOX_AGENT_REQUEST_HEADER, reformedUserAgent)
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

  private void sendBatch(List<Event> batch, Callback callback, boolean serializeNulls) {
    Gson gson = serializeNulls ? new GsonBuilder().serializeNulls().create() : new Gson();
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
      .addHeader(MAPBOX_AGENT_REQUEST_HEADER, reformedUserAgent)
      .post(body)
      .build();

    OkHttpClient client = setting.getClient(certificateBlacklist, batch.size());
    client.newCall(request).enqueue(callback);
  }

  private boolean isExtraDebuggingNeeded() {
    return true;
  }

  private RequestBody reverseMultiForm(MultipartBody.Builder builder) {
    MultipartBody multipartBody = builder.build();

    builder = new MultipartBody.Builder(BOUNDARY)
      .setType(MultipartBody.FORM);

    for (int i = multipartBody.size() - 1; i > -1; i--) {
      builder.addPart(multipartBody.part(i));
    }

    return builder.build();
  }

  synchronized void setBaseUrl(String eventsHost) {
    HttpUrl baseUrl = TelemetryClientSettings.configureUrlHostname(eventsHost);
    setting = setting.toBuilder().baseUrl(baseUrl).build();
  }
}
