package com.mapbox.android.telemetry;


import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientLocationEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingLocationEvent() throws Exception {
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    double aLatitude = 40.416775;
    double aLongitude = -3.703790;
    Event aLocationEvent = new LocationEvent("aSessionId", aLatitude, aLongitude);
    List<Event> theLocationEvent = obtainEvents(aLocationEvent);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theLocationEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theLocationEvent.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }
}
