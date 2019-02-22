package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

class UploadClientFactory {
  private static final String LOG_TAG = "TelemetryClientFactory";
  private static final String RETRIEVING_APP_META_DATA_ERROR_MESSAGE = "Failed when retrieving app meta-data: %s";
  private final String accessToken;
  private final String userAgent;
  private final CertificateBlacklist certificateBlacklist;
  private final Context context;
  private final Map<Environment, UploadClientBuild> BUILD_TELEMETRY_CLIENT = new HashMap<Environment,
    UploadClientBuild>() {
    {
      put(Environment.CHINA, new UploadClientBuild() {
        @Override
        public UploadClient build(ServerInformation serverInformation) {
          return buildUploadClient(Environment.CHINA, certificateBlacklist);
        }
      });
      put(Environment.STAGING, new UploadClientBuild() {
        @Override
        public UploadClient build(ServerInformation serverInformation) {
          return buildUploadClientCustom(serverInformation, certificateBlacklist);
        }
      });
      put(Environment.COM, new UploadClientBuild() {
        @Override
        public UploadClient build(ServerInformation serverInformation) {
          return buildUploadClient(Environment.COM, certificateBlacklist);
        }
      });
    }
  };

  UploadClientFactory(Context context, String accessToken, String userAgent) {
    this.context = context;
    this.accessToken = accessToken;
    this.userAgent = userAgent;
    this.certificateBlacklist = new CertificateBlacklist(context);
  }

  UploadClient obtainClient() {
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
      Log.e(LOG_TAG, String.format(RETRIEVING_APP_META_DATA_ERROR_MESSAGE, exception.getMessage()));
    }
    return buildUploadClient(Environment.COM, certificateBlacklist);
  }

  private UploadClient buildUploadClient(Environment environment, CertificateBlacklist certificateBlacklist) {
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .environment(environment)
      .build();

    return new UploadClient(certificateBlacklist, context, accessToken, userAgent,
      telemetryClientSettings);
  }

  private UploadClient buildUploadClientCustom(ServerInformation serverInformation,
                                                  CertificateBlacklist certificateBlacklist) {
    Environment environment = serverInformation.getEnvironment();
    String hostname = serverInformation.getHostname();
    String accessToken = serverInformation.getAccessToken();
    TelemetryClientSettings telemetryClientSettings = new TelemetryClientSettings.Builder()
      .environment(environment)
      .baseUrl(TelemetryClientSettings.configureUrlHostname(hostname))
      .build();

    return new UploadClient(certificateBlacklist, context, accessToken, userAgent,
      telemetryClientSettings);
  }
}
