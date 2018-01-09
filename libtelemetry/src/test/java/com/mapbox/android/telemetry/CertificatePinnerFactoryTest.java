package com.mapbox.android.telemetry;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class CertificatePinnerFactoryTest {

  @Test
  public void checksStagingHostname() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> stagingCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.STAGING);

    assertTrue(stagingCertificatesPins.containsKey("cloudfront-staging.tilestream.net"));
  }

  @Test
  public void checksStagingGeotrustCertificatesPins() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> stagingCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.STAGING);

    assertTrue(stagingCertificatesPins.containsKey("cloudfront-staging.tilestream.net"));
    List<String> stagingPins = stagingCertificatesPins.get("cloudfront-staging.tilestream.net");
    assertTrue(stagingPins.contains("sha256/3euxrJOrEZI15R4104UsiAkDqe007EPyZ6eTL/XxdAY="));
    assertTrue(stagingPins.contains("sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w="));
    assertTrue(stagingPins.contains("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="));
  }

  @Test
  public void checksComHostname() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> comCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.COM);

    assertTrue(comCertificatesPins.containsKey("events.mapbox.com"));
  }

  @Test
  public void checksComGeotrustCertificatesPins() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> comCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.COM);

    assertTrue(comCertificatesPins.containsKey("events.mapbox.com"));
    List<String> stagingPins = comCertificatesPins.get("events.mapbox.com");
    assertTrue(stagingPins.contains("sha256/BhynraKizavqoC5U26qgYuxLZst6pCu9J5stfL6RSYY="));
    assertTrue(stagingPins.contains("sha256/owrR9U9FWDWtrFF+myoRIu75JwU4sJwzvhCNLZoY37g="));
    assertTrue(stagingPins.contains("sha256/SQVGZiOrQXi+kqxcvWWE96HhfydlLVqFr4lQTqI5qqo="));
    assertTrue(stagingPins.contains("sha256/yJLOJQLNTPNSOh3Btyg9UA1icIoZZssWzG0UmVEJFfA="));
  }

  @Test
  public void checksComDigiCertCertificatesPins() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> comCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.COM);

    assertTrue(comCertificatesPins.containsKey("events.mapbox.com"));
    List<String> stagingPins = comCertificatesPins.get("events.mapbox.com");
    assertTrue(stagingPins.contains("sha256/Tb0uHZ/KQjWh8N9+CZFLc4zx36LONQ55l6laDi1qtT4="));
    assertTrue(stagingPins.contains("sha256/RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho="));
    assertTrue(stagingPins.contains("sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="));
    assertTrue(stagingPins.contains("sha256/yGp2XoimPmIK24X3bNV1IaK+HqvbGEgqar5nauDdC5E="));
  }

  @Test
  public void checksChinaHostname() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> chinaCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.CHINA);

    assertTrue(chinaCertificatesPins.containsKey("events.mapbox.cn"));
  }

  @Test
  public void checksChinaDigiCertCertificatesPins() throws Exception {
    CertificatePinnerFactory certificatePinnerFactory = new CertificatePinnerFactory();

    Map<String, List<String>> chinaCertificatesPins = certificatePinnerFactory
      .provideCertificatesPinsFor(Environment.CHINA);

    assertTrue(chinaCertificatesPins.containsKey("events.mapbox.cn"));
    List<String> stagingPins = chinaCertificatesPins.get("events.mapbox.cn");
    assertTrue(stagingPins.contains("sha256/gakY+fylqW6kp6piqnaQNLZFzT8HlhzP5lsGJk5WguE="));
    assertTrue(stagingPins.contains("sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w="));
    assertTrue(stagingPins.contains("sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="));
  }
}