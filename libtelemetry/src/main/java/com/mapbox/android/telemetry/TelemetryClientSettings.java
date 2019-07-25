package com.mapbox.android.telemetry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.DEFAULT_CHINA_EVENTS_HOST;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.DEFAULT_COM_EVENTS_HOST;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.DEFAULT_STAGING_EVENTS_HOST;

class TelemetryClientSettings {
  private static final Map<Environment, String> HOSTS = new HashMap<Environment, String>() {
    {
      put(Environment.STAGING, DEFAULT_STAGING_EVENTS_HOST);
      put(Environment.COM, DEFAULT_COM_EVENTS_HOST);
      put(Environment.CHINA, DEFAULT_CHINA_EVENTS_HOST);
    }
  };
  private static final String HTTPS_SCHEME = "https";
  private final Context context;
  private Environment environment;
  private final OkHttpClient client;
  private final HttpUrl baseUrl;
  private final SSLSocketFactory sslSocketFactory;
  private final X509TrustManager x509TrustManager;
  private final HostnameVerifier hostnameVerifier;
  private boolean debugLoggingEnabled;

  TelemetryClientSettings(Builder builder) {
    this.context = builder.context;
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

  OkHttpClient getClient(CertificateBlacklist certificateBlacklist, int eventCount) {
    // Order in which interceptors are added matter!
    Interceptor[] interceptors = {
      new GzipRequestInterceptor() };
    // TODO: add network interceptors in the following order
    // new NetworkUsageInterceptor(new NetworkUsageMetricsCollector(context, metrics)),
    // new NetworkErrorInterceptor(metrics, eventCount) };
    return configureHttpClient(certificateBlacklist, interceptors);
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
    return new Builder(context)
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
    Context context;
    Environment environment = Environment.COM;
    OkHttpClient client = new OkHttpClient();
    HttpUrl baseUrl = null;
    SSLSocketFactory sslSocketFactory = null;
    X509TrustManager x509TrustManager = null;
    HostnameVerifier hostnameVerifier = null;
    boolean debugLoggingEnabled = false;

    Builder(Context context) {
      this.context = context;
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
                                           Interceptor[] interceptors) {
    CertificatePinnerFactory factory = new CertificatePinnerFactory();
    OkHttpClient.Builder builder = client.newBuilder()
      .retryOnConnectionFailure(true)
      .certificatePinner(factory.provideCertificatePinnerFor(environment, certificateBlacklist))
      .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));

    if (interceptors != null) {
      for (Interceptor interceptor: interceptors) {
        builder.addInterceptor(interceptor);
      }
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
