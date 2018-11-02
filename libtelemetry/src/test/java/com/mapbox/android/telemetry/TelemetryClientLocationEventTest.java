package com.mapbox.android.telemetry;


import android.app.ActivityManager;
import android.content.Context;

import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import okhttp3.Callback;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelemetryClientLocationEventTest extends MockWebServerTest {

  @Test
  public void sendsTheCorrectBodyPostingLocationEvent() throws Exception {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);

    TelemetryClient telemetryClient = obtainATelemetryClient("anyAccessToken", "anyUserAgent");
    double aLatitude = 40.416775;
    double aLongitude = -3.703790;
    Event aLocationEvent = new LocationEvent("aSessionId", aLatitude, aLongitude, "");
    List<Event> theLocationEvent = obtainEvents(aLocationEvent);
    Callback mockedCallback = mock(Callback.class);
    enqueueMockResponse();

    telemetryClient.sendEvents(theLocationEvent, mockedCallback);

    String expectedRequestBody = obtainExpectedRequestBody(new GsonBuilder(), theLocationEvent.get(0));
    assertRequestBodyEquals(expectedRequestBody);
  }
}
