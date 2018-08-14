package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import org.junit.Test;

import java.util.HashMap;

import okhttp3.Callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VisionEventFactoryTest {

  @Test(expected = IllegalStateException.class)
  public void checksMapboxTelemetryNotInitialized() throws Exception {
    MapboxTelemetry.applicationContext = null;

    new VisionEventFactory();
  }

  @Test
  public void checksVisionEvent() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String anyName = "anyName";
    HashMap mockedContents = mock(HashMap.class);

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, anyName, mockedContents);

    assertTrue(visonEvent instanceof VisionEvent);
  }

  @Test
  public void checksVisionType() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String anyName = "anyName";
    HashMap mockedContents = mock(HashMap.class);

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, anyName, mockedContents);

    assertEquals(Event.Type.VIS_GENERAL, visonEvent.obtainType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksVisionInvalidType() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String anyName = "anyName";
    HashMap mockedContents = mock(HashMap.class);
    Event.Type notAVisionType = Event.Type.MAP_CLICK;

    aVisionEventFactory.createVisionEvent(notAVisionType, anyName, mockedContents);
  }

  @Test
  public void checksValidName() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String validName = "validName";
    HashMap mockedContents = mock(HashMap.class);

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, validName, mockedContents);

    assertTrue(visonEvent instanceof VisionEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidName() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String nullName = null;
    HashMap mockedContents = mock(HashMap.class);

    aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, nullName, mockedContents);
  }

  @Test
  public void checksValidContents() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String anyName = "anyName";
    HashMap validContents = mock(HashMap.class);

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, anyName, validContents);

    assertTrue(visonEvent instanceof VisionEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidContents() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String anyName = "anyName";
    HashMap nullContents = null;

    aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL, anyName, nullContents);
  }

  private void initializeMapboxTelemetry() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
    MapboxTelemetry.applicationContext = mockedContext;
    String aValidAccessToken = "validAccessToken";
    String aValidUserAgent = "MapboxTelemetryAndroid/";
    EventsQueue mockedEventsQueue = mock(EventsQueue.class);
    TelemetryClient mockedTelemetryClient = mock(TelemetryClient.class);
    Callback mockedHttpCallback = mock(Callback.class);
    SchedulerFlusher mockedSchedulerFlusher = mock(SchedulerFlusher.class);
    Clock mockedClock = mock(Clock.class);
    boolean indifferentServiceBound = true;
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(false);
    TelemetryLocationEnabler telemetryLocationEnabler = new TelemetryLocationEnabler(false);
    new MapboxTelemetry(mockedContext, aValidAccessToken, aValidUserAgent,
      mockedEventsQueue, mockedTelemetryClient, mockedHttpCallback, mockedSchedulerFlusher, mockedClock,
      indifferentServiceBound, telemetryEnabler, telemetryLocationEnabler);
  }
}
