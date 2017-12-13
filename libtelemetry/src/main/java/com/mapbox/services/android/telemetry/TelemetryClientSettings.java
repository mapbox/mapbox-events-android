package com.mapbox.services.android.telemetry;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class TelemetryClientSettings {
  private static final String STAGING_EVENTS_HOST = "api-events-staging.tilestream.net";
  private static final String COM_EVENTS_HOST = "events.mapbox.com";
  private static final String CHINA_EVENTS_HOST = "events.mapbox.cn";
  private static final Map<Environment, String> HOSTS = new HashMap<Environment, String>() {
    {
      put(Environment.STAGING, STAGING_EVENTS_HOST);
      put(Environment.COM, COM_EVENTS_HOST);
      put(Environment.CHINA, CHINA_EVENTS_HOST);
    }
  };
  private static final String HTTPS_SCHEME = "https";
  private Environment environment;
  private final OkHttpClient client;
  private final HttpUrl baseUrl;
  private final SSLSocketFactory sslSocketFactory;
  private final X509TrustManager x509TrustManager;
  private boolean debugLoggingEnabled;

  TelemetryClientSettings(Builder builder) {
    this.environment = builder.environment;
    this.client = builder.client;
    this.baseUrl = builder.baseUrl;
    this.sslSocketFactory = builder.sslSocketFactory;
    this.x509TrustManager = builder.x509TrustManager;
    this.debugLoggingEnabled = builder.debugLoggingEnabled;
  }

  Environment getEnvironment() {
    return environment;
  }

  OkHttpClient getClient() {
    return configureHttpClient();
  }

  HttpUrl getBaseUrl() {
    return baseUrl;
  }

  boolean isDebugLoggingEnabled() {
    return debugLoggingEnabled;
  }

  private OkHttpClient configureHttpClient() {
    CertificatePinnerFactory factory = new CertificatePinnerFactory();
    OkHttpClient.Builder builder = client.newBuilder()
      .addInterceptor(new GzipRequestInterceptor())
      .retryOnConnectionFailure(true)
      .certificatePinner(factory.provideCertificatePinnerFor(environment))
      .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
    if (isSocketFactoryUnset(sslSocketFactory, x509TrustManager)) {
      builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
    }

    return builder.build();
  }

  private boolean isSocketFactoryUnset(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager) {
    return sslSocketFactory != null && x509TrustManager != null;
  }

  Builder toBuilder() {
    return new Builder()
      .environment(environment)
      .client(client)
      .baseUrl(baseUrl)
      .sslSocketFactory(sslSocketFactory)
      .x509TrustManager(x509TrustManager)
      .debugLoggingEnabled(debugLoggingEnabled);
  }

  public static final class Builder {
    Environment environment = Environment.COM;
    OkHttpClient client = new OkHttpClient();
    HttpUrl baseUrl = null;
    SSLSocketFactory sslSocketFactory = null;
    X509TrustManager x509TrustManager = null;
    boolean debugLoggingEnabled = false;

    public Builder() {
    }

    public Builder environment(Environment environment) {
      this.environment = environment;
      return this;
    }

    public Builder client(OkHttpClient client) {
      if (client != null) {
        this.client = client;
      }
      return this;
    }

    public Builder baseUrl(HttpUrl baseUrl) {
      if (baseUrl != null) {
        this.baseUrl = baseUrl;
      }
      return this;
    }

    public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = sslSocketFactory;
      return this;
    }

    public Builder x509TrustManager(X509TrustManager x509TrustManager) {
      this.x509TrustManager = x509TrustManager;
      return this;
    }

    public Builder debugLoggingEnabled(boolean debugLoggingEnabled) {
      this.debugLoggingEnabled = debugLoggingEnabled;
      return this;
    }

    public TelemetryClientSettings build() {
      if (baseUrl == null) {
        this.baseUrl = configureUrlHostname();
      }
      return new TelemetryClientSettings(this);
    }

    private HttpUrl configureUrlHostname() {
      HttpUrl.Builder builder = new HttpUrl.Builder().scheme(HTTPS_SCHEME);
      String eventsHost = HOSTS.get(environment);
      builder.host(eventsHost);
      return builder.build();
    }
  }
}
