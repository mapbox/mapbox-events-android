package com.mapbox.android.telemetry;


import android.os.Bundle;

class ComServerInformation implements EnvironmentResolver {

  @Override
  public void nextChain(EnvironmentResolver chain) {
  }

  @Override
  public ServerInformation obtainServerInformation(Bundle appMetaData) {
    ServerInformation com = new ServerInformation(Environment.COM);
    return com;
  }
}
