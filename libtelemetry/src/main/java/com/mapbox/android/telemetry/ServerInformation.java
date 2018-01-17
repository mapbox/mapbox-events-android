package com.mapbox.android.telemetry;


class ServerInformation {
  private Environment environment;
  private String hostname;
  private String accessToken;

  ServerInformation(Environment environment) {
    this.environment = environment;
  }

  Environment getEnvironment() {
    return environment;
  }

  String getHostname() {
    return hostname;
  }

  void setHostname(String hostname) {
    this.hostname = hostname;
  }

  String getAccessToken() {
    return accessToken;
  }

  void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
