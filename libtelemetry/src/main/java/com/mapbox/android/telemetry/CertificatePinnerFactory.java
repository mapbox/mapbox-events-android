package com.mapbox.android.telemetry;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.CertificatePinner;

class CertificatePinnerFactory {

  private static final Map<Environment, Map<String, List<String>>> CERTIFICATES_PINS = new HashMap<Environment,
    Map<String, List<String>>>() {
    {
      put(Environment.STAGING, StagingCertificatePins.CERTIFICATE_PINS);
      put(Environment.COM, ComCertificatePins.CERTIFICATE_PINS);
      put(Environment.CHINA, ChinaCertificatePins.CERTIFICATE_PINS);
    }
  };
  private static final String COM_EVENTS = "events.mapbox.com";
  private static final String CHINA_EVENTS = "events.mapbox.cn";

  /**
   * Based on http://square.github.io/okhttp/3.x/okhttp/okhttp3/CertificatePinner.html
   *
   * @return The CertificatePinner instance
   */
  CertificatePinner provideCertificatePinnerFor(Environment environment) {
    CertificatePinner.Builder certificatePinnerBuilder = new CertificatePinner.Builder();

    Map<String, List<String>> certificatesPins = provideCertificatesPinsFor(environment);
    addCertificatesPins(certificatesPins, certificatePinnerBuilder, environment);

    return certificatePinnerBuilder.build();
  }

  Map<String, List<String>> provideCertificatesPinsFor(Environment environment) {
    return CERTIFICATES_PINS.get(environment);
  }

  private void addCertificatesPins(Map<String, List<String>> pins, CertificatePinner.Builder builder, Environment environment) {
    removeBlacklistedPins(pins, environment);

    for (Map.Entry<String, List<String>> entry : pins.entrySet()) {
      for (String pin : entry.getValue()) {
        builder.add(entry.getKey(), pin);
      }
    }
  }

  private Map<String, List<String>> removeBlacklistedPins(Map<String, List<String>> pins, Environment environment) {
    String key = COM_EVENTS;

    if (environment == Environment.CHINA) {
      key = CHINA_EVENTS;
    }

    List<String> hashList = pins.get(key);
    List<String> tempList = pins.get(key);
    CertificateBlacklist certificateBlacklist = new CertificateBlacklist(MapboxTelemetry.applicationContext);

    ArrayList blackList = certificateBlacklist.retrieveBlackList();
    //    blackList.add("sha256/BhynraKizavqoC5U26qgYuxLZst6pCu9J5stfL6RSYY=");

    Log.e("PinnerFactory", "pre hashList: " + hashList);


    for(String hash: hashList) {
      if (blackList.contains(hash)) {
        tempList.remove(hash);
      }
    }

    Log.e("PinnerFactory", "post hashList: " + tempList);

    return null;
  }
}
