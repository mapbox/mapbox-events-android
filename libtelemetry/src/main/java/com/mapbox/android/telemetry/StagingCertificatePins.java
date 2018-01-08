package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StagingCertificatePins {

  static final Map<String, List<String>> CERTIFICATE_PINS = new HashMap<String, List<String>>() {
    {
      put("cloudfront-staging.tilestream.net", new ArrayList<String>() {
        {
          add("sha256/3euxrJOrEZI15R4104UsiAkDqe007EPyZ6eTL/XxdAY=");
          add("sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=");
          add("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=");
        }
      });
    }
  };
}