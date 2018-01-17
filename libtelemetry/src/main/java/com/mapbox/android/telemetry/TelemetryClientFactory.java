package com.mapbox.android.telemetry;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

class TelemetryClientFactory {
  private static final String LOG_TAG = "TelemetryClientFactory";
  private static final String RETRIEVING_APP_META_DATA_ERROR_MESSAGE = "Failed when retrieving app meta-data: %s";
  private final String accessToken;
  private final String userAgent;
  private final Logger logger;
  private final Map<Environment, TelemetryClientBuild> BUILD_TELEMETRY_CLIENT = new HashMap<Environment,
    TelemetryClientBuild>() {
    {
      put(Environment.CHINA, new TelemetryClientBuild() {
        @Override
        public TelemetryClient build(ServerInformation serverInformation) {
          return buildTelemetryClient(Environment.CHINA);
        }
      });
      put(Environment.STAGING, new TelemetryClientBuild() {
        @Override
        public TelemetryClient build(ServerInformation serverInformation) {
          return buildTelemetryClientCustom(serverInformation);
        }
      });
      put(Environment.COM, new TelemetryClientBuild() {
        @Override
        public TelemetryClient build(ServerInformation serverInformation) {
          return buildTelemetryClient(Environment.COM);
        }
      });
    }
  };

  TelemetryClientFactory(String accessToken, String userAgent, Logger logger) {
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.logger = logger;
  }

  TelemetryClient obtainTelemetryClient(Context context) {
    EnvironmentChain environmentChain = new EnvironmentChain();
    EnvironmentResolver setupChain = environmentChain.setup();
    ServerInformation serverInformation;
    try {
      ApplicationInfo appInformation = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);
      if (appInformation != null && appInformation.metaData != null) {
        serverInformation = setupChain.obtainServerInformation(appInformation.metaData);
        return BUILD_TELEMETRY_CLIENT.get(serverInformation.getEnvironment()).build(serverInformation);
      }
    } catch (Exception exception) {
      logger.error(LOG_TAG, String.format(RETRIEVING_APP_META_DATA_ERROR_MESSAGE, exception.getMessage()));
    }
    return buildTelemetryClient(Environment.COM);
  }

  private TelemetryClient buildTelemetryClient(Environment environment) {
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .environment(environment)
      .build();
    TelemetryClient telemetryClient = new TelemetryClient(accessToken, userAgent, telemetryClientSettings, logger);

    return telemetryClient;
  }

  private TelemetryClient buildTelemetryClientCustom(ServerInformation serverInformation) {
    Environment environment = serverInformation.getEnvironment();
    String hostname = serverInformation.getHostname();
    String accessToken = serverInformation.getAccessToken();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .environment(environment)
      .baseUrl(TelemetryClientSettings.configureUrlHostname(hostname))
      .build();
    TelemetryClient telemetryClient = new TelemetryClient(accessToken, userAgent, telemetryClientSettings, logger);

    return telemetryClient;
  }
}
