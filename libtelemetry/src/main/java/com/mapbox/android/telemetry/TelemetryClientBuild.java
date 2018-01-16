package com.mapbox.android.telemetry;

interface TelemetryClientBuild {

  TelemetryClient build(ServerInformation serverInformation);
}
