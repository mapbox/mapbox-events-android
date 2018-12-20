package com.mapbox.android.telemetry;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.CertificatePinner;

class CertificatePinnerFactory {
  private static final String SHA256_PIN_FORMAT = "sha256/%s";

  private static final Map<Environment, Map<String, List<String>>> CERTIFICATES_PINS = new HashMap<Environment,
    Map<String, List<String>>>() {
    {
      put(Environment.STAGING, StagingCertificatePins.CERTIFICATE_PINS);
      put(Environment.COM, ComCertificatePins.CERTIFICATE_PINS);
      put(Environment.CHINA, ChinaCertificatePins.CERTIFICATE_PINS);
    }
  };

  /**
   * Based on http://square.github.io/okhttp/3.x/okhttp/okhttp3/CertificatePinner.html
   *
   * @return The CertificatePinner instance
   */
  CertificatePinner provideCertificatePinnerFor(Environment environment, CertificateBlacklist certificateBlacklist) {
    CertificatePinner.Builder certificatePinnerBuilder = new CertificatePinner.Builder();
    Map<String, List<String>> certificatesPins = provideCertificatesPinsFor(environment);
    certificatesPins = removeBlacklistedPins(certificatesPins, certificateBlacklist);
    addCertificatesPins(certificatesPins, certificatePinnerBuilder);
    return certificatePinnerBuilder.build();
  }

  Map<String, List<String>> provideCertificatesPinsFor(Environment environment) {
    return CERTIFICATES_PINS.get(environment);
  }

  private void addCertificatesPins(Map<String, List<String>> pins, CertificatePinner.Builder builder) {
    for (Map.Entry<String, List<String>> entry : pins.entrySet()) {
      for (String pin : entry.getValue()) {
        builder.add(entry.getKey(), String.format(SHA256_PIN_FORMAT, pin));
      }
    }
  }

  private Map<String, List<String>> removeBlacklistedPins(Map<String, List<String>> pins,
                                                          CertificateBlacklist certificateBlacklist) {
    String key = retrievePinKey(pins);
    List<String> hashList = pins.get(key);
    if (hashList != null) {
      hashList = removeBlacklistedHashes(certificateBlacklist, hashList);
      pins.put(key, hashList);
    }
    return pins;
  }

  private String retrievePinKey(Map<String, List<String>> pins) {
    Set<String> pinsKey = pins.keySet();
    return pinsKey.iterator().next();
  }

  private List<String> removeBlacklistedHashes(CertificateBlacklist certificateBlacklist, List<String> hashList) {
    for (String hash : hashList) {
      if (certificateBlacklist.isBlacklisted(hash)) {
        hashList.remove(hash);
      }
    }
    return hashList;
  }
}
