package com.mapbox.android.telemetry;


import android.os.Bundle;

class StagingServerInformation implements EnvironmentResolver {
  private static final String KEY_META_DATA_STAGING_SERVER = "com.mapbox.TestEventsServer";
  private static final String KEY_META_DATA_STAGING_ACCESS_TOKEN = "com.mapbox.TestEventsAccessToken";
  private EnvironmentResolver chain;

  @Override
  public void nextChain(EnvironmentResolver chain) {
    this.chain = chain;
  }

  @Override
  public ServerInformation obtainServerInformation(Bundle appMetaData) {
    String hostname = appMetaData.getString(KEY_META_DATA_STAGING_SERVER);
    String accessToken = appMetaData.getString(KEY_META_DATA_STAGING_ACCESS_TOKEN);

    if (!TelemetryUtils.isEmpty(hostname) && !TelemetryUtils.isEmpty(accessToken)) {
      ServerInformation staging = obtainStagingServerInformation(hostname, accessToken);
      return staging;
    } else {
      return chain.obtainServerInformation(appMetaData);
    }
  }

  private ServerInformation obtainStagingServerInformation(String hostname, String accessToken) {
    ServerInformation staging = new ServerInformation(Environment.STAGING);
    staging.setHostname(hostname);
    staging.setAccessToken(accessToken);
    return staging;
  }
}
