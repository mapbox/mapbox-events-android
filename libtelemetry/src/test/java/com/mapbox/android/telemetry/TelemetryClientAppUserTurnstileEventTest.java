package com.mapbox.android.telemetry;


import android.content.Context;

import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.mock;

public class TelemetryClientAppUserTurnstileEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    Context mockedContext = TelemetryClientTest.getMockedContext();
    MapboxTelemetry.applicationContext = mockedContext;
    String anyUserAgent = "anyUserAgent";
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", anyUserAgent,
      anyUserAgent , mockedContext);
    Event anAppUserTurnstile = new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
    List<Event> theAppUserTurnstile = obtainEvents(anAppUserTurnstile);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theAppUserTurnstile, mockedCallback, true);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(),
      true, theAppUserTurnstile.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }
}
