package com.mapbox.android.telemetry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChinaCertificatePins {

  static final Map<String, List<String>> CERTIFICATE_PINS = new HashMap<String, List<String>>() {
    {
      put("events.mapbox.cn", new ArrayList<String>() {
        {
          //old certificates
          add("sha256/gakY+fylqW6kp6piqnaQNLZFzT8HlhzP5lsGJk5WguE=");
          add("sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=");
          add("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=");

          //new digicert certificates
          add("sha256/3coVlMAEAYhOEJHgXwloiPDGaF+ZfxHZbVoK8AYYWVg=");
          add("sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=");
          add("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=");

          //new GeoTrust Certs
          add("sha256/+O+QJCmvoB/FkTd0/5FvmMSvFbMqjYU+Txrw1lyGkUQ=");
          add("sha256/zUIraRNo+4JoAYA7ROeWjARtIoN4rIEbCpfCRQT6N6A=");
          add("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=");
        }
      });
    }
  };
}