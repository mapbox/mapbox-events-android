package com.mapbox.android.telemetry;


import android.content.Context;
import android.net.ConnectivityManager;

import com.google.gson.GsonBuilder;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TelemetryClientAppUserTurnstileEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingAppUserTurnstileEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    final ConnectivityManager manager = mock(ConnectivityManager.class);
    Mockito.when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
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
