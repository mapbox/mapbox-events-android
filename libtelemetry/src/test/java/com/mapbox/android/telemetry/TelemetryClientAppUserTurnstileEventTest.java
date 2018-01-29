package com.mapbox.android.telemetry;


import android.content.Context;

import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TelemetryClientAppUserTurnstileEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    Event anAppUserTurnstile = new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
    List<Event> theAppUserTurnstile = obtainEvents(anAppUserTurnstile);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theAppUserTurnstile, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theAppUserTurnstile.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }
}
