package com.mapbox.services.android.telemetry;


import com.google.gson.GsonBuilder;

import org.junit.Test;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientLocationEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingLocationEvent() throws Exception {
    TelemetryClient telemetryClient = obtainDefaultTelemetryClient();
    double aLatitude = 40.416775;
    double aLongitude = -3.703790;
    Event theLocationEvent = new LocationEvent("aSessionId", aLatitude, aLongitude);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvent(theLocationEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theLocationEvent);
    assertRequestBodyEquals(expectedRequestBody);
  }
}
