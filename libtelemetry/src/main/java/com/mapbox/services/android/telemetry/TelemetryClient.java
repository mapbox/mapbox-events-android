package com.mapbox.services.android.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

class TelemetryClient {
  private static final String LOG_TAG = "TelemetryClient";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String EVENTS_ENDPOINT = "/events/v2";
  private static final String USER_AGENT_REQUEST_HEADER = "User-Agent";
  private static final String ACCESS_TOKEN_QUERY_PARAMETER = "access_token";

  private String accessToken = null;
  private String userAgent = null;
  private final TelemetryClientSettings setting;
  private final Logger logger;

  TelemetryClient(String accessToken, String userAgent, TelemetryClientSettings setting, Logger logger) {
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.setting = setting;
    this.logger = logger;
  }

  void updateUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  void sendEvents(List<Event> events, Callback callback) {
    ArrayList<Event> batch = new ArrayList<>();
    batch.addAll(events);
    sendBatch(batch, callback);
  }

  // TODO Remove send a single event method. Use sendEvents instead
  void sendEvent(Event event, Callback callback) throws IOException {
    ArrayList<Event> oneEvent = new ArrayList<>();
    oneEvent.add(event);
    sendBatch(oneEvent, callback);
  }

  private void sendBatch(List<Event> batch, Callback callback) {
    GsonBuilder gsonBuilder = configureGsonBuilder();
    Gson gson = gsonBuilder.create();
    String payload = gson.toJson(batch);
    RequestBody body = RequestBody.create(JSON, payload);
    HttpUrl baseUrl = setting.getBaseUrl();

    HttpUrl url = baseUrl.newBuilder(EVENTS_ENDPOINT)
      .addQueryParameter(ACCESS_TOKEN_QUERY_PARAMETER, accessToken).build();

    // Extra debug in staging mode
    if (setting.getEnvironment().equals(Environment.STAGING)) {
      logger.debug(LOG_TAG, String.format("Sending POST to %s with %d event(s) (user agent: %s) with "
        + "payload: %s", url, batch.size(), userAgent, payload));
    }

    Request request = new Request.Builder()
      .url(url)
      .header(USER_AGENT_REQUEST_HEADER, userAgent)
      .post(body)
      .build();

    OkHttpClient client = setting.getClient();
    client.newCall(request).enqueue(callback);
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
