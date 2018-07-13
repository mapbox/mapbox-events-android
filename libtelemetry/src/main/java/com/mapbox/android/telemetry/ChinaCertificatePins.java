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

          add("sha256/T4XyKSRwZ5icOqGmJUXiDYGa+SaXKTGQXZwhqpwNTEo=");
          add("sha256/KlV7emqpeM6V2MtDEzSDzcIob6VwkdWHiVsNQQzTIeo=");
          add("sha256/16TK3iq9ZB4AukmDemjUyhcPTUnsSuqd5OB5zOrheZY=");
          add("sha256/F16cPFncMDkB4XbRfK64H1dqncNg6JOdd+w2qElR/hM=");
          add("sha256/45PQwWtFAHQd/cVzMVuhkwOQwCF+JE4ZViA4gkzvWeQ=");
          add("sha256/mCzfopN5vqaerktI/172w8T7qw2sfRXgUL4Z7xA2e/c=");
          add("sha256/rFFCqIOOKu7KH1v73IHb6mzZQth7cVvVDaH+EjkNfKM=");
          add("sha256/pZBpEiH9vLwSICbogdpgyGG3NCjVKw4oG2rEWRf03Vk=");
          add("sha256/gPgpSOzaLjbIpDLlh302x2irQTzWfsQqIWhUsreMMzI=");
          add("sha256/wLHqvUDDQfFymRVS2O6AF5nkuAY+KJZpI4+pohK+SUE=");
          add("sha256/yAEZR9ydeTrMlcUp91PHdmJ3lBa86IWsKRwiM0KzS6Q=");
          add("sha256/k3NZbP68SikfwacfWDm4s3YJDsPVWJSOF4GlCWo5RJA=");
          add("sha256/1PRG2KOhfDE+xMS1fxft5CtQO99mzqhpl4gPz/64IxQ=");
          add("sha256/FBibSsaWfYYIkij1x4Oc9Lt0jHl+6AhBTWAypcOphhc=");
          add("sha256/X0K6GmWp00Pb0YATdlCPeXaZR/NxxHTv41OAEkymkbU=");
          add("sha256/DU/+Q9Itbb4WuSfuTvOgPtxtF6eAbTH7pUFn17/o5E0=");
          add("sha256/BYGHyEqtaJEZn+02i4jy4dGRRFNr6xckQjTL7DMZFes=");
          add("sha256/zr1/pj8y4FUbrxIYRaHVZWvhsMPzDVW0R+ljPHrX5Sw=");
          add("sha256/fS9IR9OWsirEnSAqParPG0BzZJ+Dk4CiHfPv1vEjrf0=");
          add("sha256/f1B7KmHknBSXNjTC8ac/Hf7hwU2goerE53TJppr0OH0=");
          add("sha256/OKbbVU/+cTlszrJkxKaQraFAoVyjPOqa5Uq8Ndd4AUg=");
          add("sha256/I0xGZF5s9kGHJHz6nKN+nYJKwf8ev1MdWkGt7EI7A7g=");
          add("sha256/anATIIIqUd4o7Asto7X7OEJ+m7YTUr0aJKHZXqL92w0=");
          add("sha256/JXFJ+lQK4GwJpJlHSZ2ZAR5luZDwMdaa2hJyhqHc1L8=");
          add("sha256/64k4IzkPceL/hQywCCvJLQds8FPMPwtclhFOR/taKAQ=");
          add("sha256/c079Pt5XXCwSv+pROEF+YW5gRoyzJ248bPxVLrUYkHM=");
          add("sha256/46ofOPUGR3SYcMB+MmXqowYKan/c18LBTV2sAk13WKc=");
          add("sha256/4qwz7KaBHxEX+YxO8STVowTg2BxlOd98GNU5feRjdjU=");
          add("sha256/hp54/fY89ziuBBp1zv3YaC8H9/G8/Xp97hdzRVdcqQ0=");
          add("sha256/BliQkuPecuHEp3FN3r1HogAkmsLtZz3ZImqLSpJoJzs=");
          add("sha256/GayCH1YATG/OS5h1bq79XRmcq/aqwoObu2OYfPN7vQc=");
          add("sha256/fW6I4HEBwa1Pwi1dldkb+ljs4re5ZY2JbsCiCxCOCgI=");
          add("sha256/GcqilfT04N2efVIWlzJWO04gdpwYC4sLnOx3TJIKA9E=");
          add("sha256/+1CHLRDE6ehp61cm8+NDMvd32z0Qc4bgnZRLH0OjE94=");
          add("sha256/4vJWNxtoMLAY35dbzKeDI+4IAFOW97WNkTWnNMtY5TA=");
          add("sha256/1YjWX9tieIA1iGkJhm7UapH6PiwGViZBWrXA3UJUAWc=");
          add("sha256/X+RKpA7gtptrZ9yI1C96Isw5RV8dQyx5z7I/xfCaBl8=");
          add("sha256/hqFsdAuHVvjX3NuaUBVZao94V30SdXLAsG1O0ajgixw=");
          add("sha256/wYl9ZFQd2LWKfjDuEQxo7S0CcrPkP9A3vb20fbHf1ZQ=");
          add("sha256/Y3ax6OgoQkcStQZ2hrIAqMDbaEEwX6xZfMZEnVcn/4k=");
          add("sha256/taSOM7qPorxZ64Whrl5ZiNCGlZqLrVPOIBwPr/Nkw6U=");
          add("sha256/KB5X/PyAAiRc7W/NjUyd6xbDdibuOTWBJB2MqHHF/Ao=");
          add("sha256/hRQ7yTW/P5l76uNNP3MXNgshlmcbDNHMtBxCbUtGAWE=");
          add("sha256/AoclhkrtKF+qHKKq0wUS4oXLwlJtWlywtiLndnNzS2U=");
          add("sha256/5ikvGB5KkNlwesHRqjYvkZGlxP6OLMbaCkpflTM4DNM=");
          add("sha256/qK2GksTrZ7LXDBkNWH6FnuNGxgxPpwNSK+NgknU7H1U=");
          add("sha256/K3qyQniCBiGmfutYDE7ryDY2YoTORgp4DOgK1laOqfo=");
          add("sha256/B7quINbFSUen02LQ9kwtYXnsJtixTpKafzXFkcRb7RU=");
          add("sha256/Kc7lrHTlRfLaeRaEof6mKKmBH2eYHMYkxOy3yGlzUWg=");
          add("sha256/7s1BUHi/AW/beA2jXamNTUgbDMH4gVPR9diIhnN1o0Q=");
        }
      });
    }
  };
}