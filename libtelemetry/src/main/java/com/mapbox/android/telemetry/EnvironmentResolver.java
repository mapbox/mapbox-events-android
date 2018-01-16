package com.mapbox.android.telemetry;


import android.os.Bundle;

interface EnvironmentResolver {
  void nextChain(EnvironmentResolver chain);

  ServerInformation obtainServerInformation(Bundle appMetaData);
}