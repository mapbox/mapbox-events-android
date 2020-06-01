package com.mapbox.android.telemetry;


import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

class ComServerInformation implements EnvironmentResolver {
  private final String LOG_TAG = "ComServerInformation";
  private final String DIGEST = "SHA-256";
  private final String KEY_META_DATA_COM_SERVER = "com.mapbox.ComEventsServer";
  private List<String> configurationList =
    new ArrayList<String>() {
      {
        add("FVQ3CP/SEI8eLPxHJnjyew2P5DTC1OBKK4Y6XkmC0WI=");
      }
    };

  @Override
  public void nextChain(EnvironmentResolver chain) {
  }

  @Override
  public ServerInformation obtainServerInformation(Bundle appMetaData) {
    ServerInformation com = new ServerInformation(Environment.COM);
    String hostname = appMetaData.getString(KEY_META_DATA_COM_SERVER);
    if (!TelemetryUtils.isEmpty(hostname)) {
      String hostnameHash = obtainHash(hostname);
      if (!TelemetryUtils.isEmpty(hostnameHash)
        && configurationList.contains(hostnameHash)) {
        com.setHostname(hostname);
      }
    }
    return com;
  }

  private String obtainHash(String hostname) {
    String hostNameHash = null;
    try {
      byte[] digest = MessageDigest.getInstance(DIGEST).digest(hostname.getBytes());
      hostNameHash = Base64.encodeToString(digest, Base64.NO_WRAP);
    } catch (Exception exception) {
      Log.d(LOG_TAG, String.format("Hostname error %s", exception.getMessage()));
    }

    return hostNameHash;
  }

  @VisibleForTesting
  void setConfigurationList(List<String> configurationList) {
    this.configurationList = configurationList;
  }

}
