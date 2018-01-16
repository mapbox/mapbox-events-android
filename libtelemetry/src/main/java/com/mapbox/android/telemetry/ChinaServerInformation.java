package com.mapbox.android.telemetry;


import android.os.Bundle;

class ChinaServerInformation implements EnvironmentResolver {
  private static final String KEY_META_DATA_CN_SERVER = "com.mapbox.CnEventsServer";
  private EnvironmentResolver chain;

  @Override
  public void nextChain(EnvironmentResolver chain) {
    this.chain = chain;
  }

  @Override
  public ServerInformation obtainServerInformation(Bundle appMetaData) {
    boolean cnServer = appMetaData.getBoolean(KEY_META_DATA_CN_SERVER);
    if (cnServer) {
      ServerInformation china = new ServerInformation(Environment.CHINA);
      return china;
    } else {
      return chain.obtainServerInformation(appMetaData);
    }
  }
}
