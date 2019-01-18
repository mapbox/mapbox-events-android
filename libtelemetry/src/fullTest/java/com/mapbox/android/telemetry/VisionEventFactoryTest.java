package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import org.junit.Test;

import okhttp3.Callback;
import okhttp3.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VisionEventFactoryTest {

  @Test(expected = IllegalStateException.class)
  public void checksMapboxTelemetryNotInitialized() {
    MapboxTelemetry.applicationContext = null;

    new VisionEventFactory();
  }

  @Test
  public void checksVisionEvent() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL);

    assertTrue(visonEvent instanceof VisionEvent);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void checksVisionObjectDetectionEvent() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    aVisionEventFactory.createVisionEvent(Event.Type.VIS_OBJ_DETECTION);
  }

  @Test
  public void checksVisionsAttachmentEvent() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    String filepath = "aFilepath";
    MediaType mediaType = mock(MediaType.class);
    AttachmentMetadata attachmentMetadata = mock(AttachmentMetadata.class);

    FileAttachment visionAttachment = aVisionEventFactory.createFileAttachment(filepath, mediaType, attachmentMetadata);

    assertTrue(visionAttachment instanceof FileAttachment);
  }

  @Test
  public void checksAttachmentEvent() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();

    Event visonEvent = aVisionEventFactory.createAttachment(Event.Type.VIS_ATTACHMENT);

    assertTrue(visonEvent instanceof Attachment);
  }

  @Test
  public void checksVisionType() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();

    Event visonEvent = aVisionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL);

    assertEquals(Event.Type.VIS_GENERAL, visonEvent.obtainType());
  }

  @Test
  public void checksAttachmentType() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();

    Attachment visionAttachment = aVisionEventFactory.createAttachment(Event.Type.VIS_ATTACHMENT);

    assertEquals(Event.Type.VIS_ATTACHMENT, visionAttachment.obtainType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksVisionInvalidType() {
    initializeMapboxTelemetry();
    VisionEventFactory aVisionEventFactory = new VisionEventFactory();
    Event.Type notAVisionType = Event.Type.MAP_CLICK;

    aVisionEventFactory.createVisionEvent(notAVisionType);
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
