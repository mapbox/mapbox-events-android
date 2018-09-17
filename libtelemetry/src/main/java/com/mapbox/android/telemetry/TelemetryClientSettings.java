package com.mapbox.android.telemetry;


import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

class TelemetryClientSettings {
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
  private final HostnameVerifier hostnameVerifier;
  private boolean debugLoggingEnabled;

  TelemetryClientSettings(Builder builder) {
    this.environment = builder.environment;
    this.client = builder.client;
    this.baseUrl = builder.baseUrl;
    this.sslSocketFactory = builder.sslSocketFactory;
    this.x509TrustManager = builder.x509TrustManager;
    this.hostnameVerifier = builder.hostnameVerifier;
    this.debugLoggingEnabled = builder.debugLoggingEnabled;
  }

  Environment getEnvironment() {
    return environment;
  }

  OkHttpClient getClient(CertificateBlacklist certificateBlacklist) {
    return configureHttpClient(certificateBlacklist, new GzipRequestInterceptor());
  }

  OkHttpClient getAttachmentClient(CertificateBlacklist certificateBlacklist) {
    return configureHttpClient(certificateBlacklist, null);
  }

  HttpUrl getBaseUrl() {
    return baseUrl;
  }

  boolean isDebugLoggingEnabled() {
    return debugLoggingEnabled;
  }

  Builder toBuilder() {
    return new Builder()
      .environment(environment)
      .client(client)
      .baseUrl(baseUrl)
      .sslSocketFactory(sslSocketFactory)
      .x509TrustManager(x509TrustManager)
      .hostnameVerifier(hostnameVerifier)
      .debugLoggingEnabled(debugLoggingEnabled);
  }

  static HttpUrl configureUrlHostname(String eventsHost) {
    HttpUrl.Builder builder = new HttpUrl.Builder().scheme(HTTPS_SCHEME);
    builder.host(eventsHost);
    return builder.build();
  }

  static final class Builder {
    Environment environment = Environment.COM;
    OkHttpClient client = new OkHttpClient();
    HttpUrl baseUrl = null;
    SSLSocketFactory sslSocketFactory = null;
    X509TrustManager x509TrustManager = null;
    HostnameVerifier hostnameVerifier = null;
    boolean debugLoggingEnabled = false;

    Builder() {
    }

    Builder environment(Environment environment) {
      this.environment = environment;
      return this;
    }

    Builder client(OkHttpClient client) {
      if (client != null) {
        this.client = client;
      }
      return this;
    }

    Builder baseUrl(HttpUrl baseUrl) {
      if (baseUrl != null) {
        this.baseUrl = baseUrl;
      }
      return this;
    }

    Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.sslSocketFactory = sslSocketFactory;
      return this;
    }

    Builder x509TrustManager(X509TrustManager x509TrustManager) {
      this.x509TrustManager = x509TrustManager;
      return this;
    }

    Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = hostnameVerifier;
      return this;
    }

    Builder debugLoggingEnabled(boolean debugLoggingEnabled) {
      this.debugLoggingEnabled = debugLoggingEnabled;
      return this;
    }

    TelemetryClientSettings build() {
      if (baseUrl == null) {
        String eventsHost = HOSTS.get(environment);
        this.baseUrl = configureUrlHostname(eventsHost);
      }
      return new TelemetryClientSettings(this);
    }
  }

  private OkHttpClient configureHttpClient(CertificateBlacklist certificateBlacklist,
                                           @Nullable Interceptor interceptor) {
    CertificatePinnerFactory factory = new CertificatePinnerFactory();
    OkHttpClient.Builder builder = client.newBuilder()
      .retryOnConnectionFailure(true)
      .certificatePinner(factory.provideCertificatePinnerFor(environment, certificateBlacklist))
      .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));

    if (interceptor != null) {
      builder.addInterceptor(interceptor);
    }

    if (isSocketFactoryUnset(sslSocketFactory, x509TrustManager)) {
      builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
      builder.hostnameVerifier(hostnameVerifier);
    }

    return builder.build();
  }

  private boolean isSocketFactoryUnset(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager) {
    return sslSocketFactory != null && x509TrustManager != null;
  }
}
