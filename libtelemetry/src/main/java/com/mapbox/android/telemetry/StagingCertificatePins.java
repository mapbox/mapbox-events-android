package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StagingCertificatePins {

  static final Map<String, List<String>> CERTIFICATE_PINS = new HashMap<String, List<String>>() {
    {
      put("api-events-staging.tilestream.net", new ArrayList<String>() {
        {
          add("3euxrJOrEZI15R4104UsiAkDqe007EPyZ6eTL/XxdAY=");
          add("5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=");
          add("r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=");
          add("PA1lecwXNRXY/Vpy0VN+jQEYChN4hCAF36oB0Ygx3wQ=");
          // new 2048 bit keys generated in April 2021
          add("8apXPecP7X3vUGqi/B42cig4O1BjQUM4dng5gMVOiK0=");
          add("MxGjtNVZ0mEdjhhfvAcTNZd+lC8WY8vKkkaSFE2azXQ=");
          add("i/5fi5jB13JKeiZJMFNu4XSIaaCNmxAWsWvmMsI7t5s=");
          add("4YJLMcE66WP2/FRID2HT0QpQRNjG7zqz/dJzP3BGct8=");
          add("H1YTKuZacKUYyGnQFVPcarkqYxvGJ7QKb9dFz2TssKw=");
        }
      });
    }
  };
}
