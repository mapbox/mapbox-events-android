package com.mapbox.android.telemetry;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MapboxTelemetry {
  private static final String NON_NULL_APPLICATION_CONTEXT_REQUIRED = "Non-null application context required.";
  private String accessToken;
  private String userAgent;
  private TelemetryClient telemetryClient;
  private Callback httpCallback;
  private CopyOnWriteArraySet<TelemetryListener> telemetryListeners = null;
  private final CertificateBlacklist certificateBlacklist;
  private final ConfigurationClient configurationClient;
  static Context applicationContext = null;

  @Deprecated
  public MapboxTelemetry(Context context, String accessToken, String userAgent) {
    initializeContext(context);
    // FIXME: Propagate certificate blacklist changes from full version
    this.configurationClient = new ConfigurationClient(context, TelemetryUtils.createFullUserAgent(context),
      accessToken, new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    checkRequiredParameters(accessToken);
    initializeTelemetryListeners();
    // Initializing callback after listeners object is instantiated
    this.httpCallback = getHttpCallback(telemetryListeners);
  }

  public MapboxTelemetry(Context context, String accessToken) {
    initializeContext(context);
    // FIXME: Propagate certificate blacklist changes from full version
    this.configurationClient = new ConfigurationClient(context, TelemetryUtils.createFullUserAgent(context),
      accessToken, new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    checkRequiredParameters(accessToken);
    initializeTelemetryListeners();
    // Initializing callback after listeners object is instantiated
    this.httpCallback = getHttpCallback(telemetryListeners);
  }

  // For testing only
  MapboxTelemetry(Context context, String accessToken, EventsQueue queue,
                  TelemetryClient telemetryClient, Callback httpCallback, SchedulerFlusher schedulerFlusher,
                  Clock clock, boolean isServiceBound, TelemetryEnabler telemetryEnabler) {
    initializeContext(context);
    checkRequiredParameters(accessToken);
    this.telemetryClient = telemetryClient;
    this.httpCallback = httpCallback;
    initializeTelemetryListeners();
    this.configurationClient = new ConfigurationClient(context, TelemetryUtils.createFullUserAgent(context),
      accessToken, new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
  }

  public boolean push(Event event) {
    if (!Event.Type.TURNSTILE.equals(event.obtainType())) {
      return false;
    }

    List<Event> appUserTurnstile = new ArrayList<>(1);
    appUserTurnstile.add(event);
    sendEvents(appUserTurnstile);
    return true;
  }

  public boolean enable() {
    if (TelemetryEnabler.isEventsEnabled(applicationContext)) {
      return true;
    }
    return false;
  }

  public boolean disable() {
    if (TelemetryEnabler.isEventsEnabled(applicationContext)) {
      return true;
    }
    return false;
  }

  public boolean updateSessionIdRotationInterval(SessionInterval interval) {
    // noop
    return false;
  }

  public void updateDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
    if (telemetryClient != null) {
      telemetryClient.updateDebugLoggingEnabled(isDebugLoggingEnabled);
    }
  }

  @Deprecated
  public void updateUserAgent(String userAgent) {

  }

  public boolean updateAccessToken(String accessToken) {
    if (isAccessTokenValid(accessToken) && updateTelemetryClient(accessToken)) {
      this.accessToken = accessToken;
      return true;
    }
    return false;
  }

  public boolean addTelemetryListener(TelemetryListener listener) {
    return telemetryListeners.add(listener);
  }

  public boolean removeTelemetryListener(TelemetryListener listener) {
    return telemetryListeners.remove(listener);
  }

  public boolean addAttachmentListener(AttachmentListener listener) {
    // noop
    return false;
  }

  public boolean removeAttachmentListener(AttachmentListener listener) {
    // noop
    return false;
  }

  // Package private (no modifier) for testing purposes
  boolean checkRequiredParameters(String accessToken) {
    boolean areValidParameters = areRequiredParametersValid(accessToken);
    if (areValidParameters) {
      initializeTelemetryClient();
    }
    return areValidParameters;
  }

  private void initializeContext(Context context) {
    if (applicationContext == null) {
      if (context != null && context.getApplicationContext() != null) {
        applicationContext = context.getApplicationContext();
      } else {
        throw new IllegalArgumentException(NON_NULL_APPLICATION_CONTEXT_REQUIRED);
      }
    }
  }

  private boolean areRequiredParametersValid(String accessToken) {
    return isAccessTokenValid(accessToken);
  }

  private boolean isAccessTokenValid(String accessToken) {
    if (!TelemetryUtils.isEmpty(accessToken)) {
      this.accessToken = accessToken;
      return true;
    }
    return false;
  }

  private void initializeTelemetryClient() {
    if (telemetryClient == null) {
      telemetryClient = createTelemetryClient(accessToken);
    }
  }

  private TelemetryClient createTelemetryClient(String accessToken) {
    String fullUserAgent = TelemetryUtils.createFullUserAgent(applicationContext);
    TelemetryClientFactory telemetryClientFactory = new TelemetryClientFactory(accessToken, fullUserAgent,
      new Logger(), certificateBlacklist);
    telemetryClient = telemetryClientFactory.obtainTelemetryClient(applicationContext);
    return telemetryClient;
  }

  private boolean updateTelemetryClient(String accessToken) {
    if (telemetryClient != null) {
      telemetryClient.updateAccessToken(accessToken);
      return true;
    }
    return false;
  }

  private void sendEvents(List<Event> events) {
    if (checkRequiredParameters(accessToken)) {
      telemetryClient.sendEvents(events, httpCallback, true);
    }
  }

  private void initializeTelemetryListeners() {
    telemetryListeners = new CopyOnWriteArraySet<>();
  }

  private static Callback getHttpCallback(final Set<TelemetryListener> listeners) {
    return new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        for (TelemetryListener telemetryListener : listeners) {
          telemetryListener.onHttpFailure(e.getMessage());
        }
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
          body.close();
        }

        for (TelemetryListener telemetryListener : listeners) {
          telemetryListener.onHttpResponse(response.isSuccessful(), response.code());
        }
      }
    };
  }
}
