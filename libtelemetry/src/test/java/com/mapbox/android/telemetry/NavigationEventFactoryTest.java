package com.mapbox.android.telemetry;

import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NavigationEventFactoryTest {

  @Test
  public void checksNavigationDepartEvent() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event departEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_DEPART, mockedNavigationState);

    assertTrue(departEvent instanceof NavigationDepartEvent);
  }

  @Test
  public void checksDepartType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event departEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_DEPART, mockedNavigationState);

    assertEquals(Event.Type.NAV_DEPART, departEvent.obtainType());
  }

  @Test
  public void checksNavigationArriveEvent() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event arriveEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_ARRIVE, mockedNavigationState);

    assertTrue(arriveEvent instanceof NavigationArriveEvent);
  }

  @Test
  public void checksArriveType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event arriveEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_ARRIVE, mockedNavigationState);

    assertEquals(Event.Type.NAV_ARRIVE, arriveEvent.obtainType());
  }

  @Test
  public void checksNavigationCancelEvent() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event cancelEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_CANCEL, mockedNavigationState);

    assertTrue(cancelEvent instanceof NavigationCancelEvent);
  }

  @Test
  public void checksCancelType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event cancelEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_CANCEL, mockedNavigationState);

    assertEquals(Event.Type.NAV_CANCEL, cancelEvent.obtainType());
  }

  @Test
  public void checksNavigationRerouteEvent() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event rerouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_REROUTE, mockedNavigationState);

    assertTrue(rerouteEvent instanceof NavigationRerouteEvent);
  }

  @Test
  public void checksRerouteType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event rerouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_REROUTE, mockedNavigationState);

    assertEquals(Event.Type.NAV_REROUTE, rerouteEvent.obtainType());
  }

  @Test
  public void checksNavigationFeedbackEvent() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event feedbackEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FEEDBACK, mockedNavigationState);

    assertTrue(feedbackEvent instanceof NavigationFeedbackEvent);
  }

  @Test
  public void checksFeedbackType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);

    Event feedbackEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FEEDBACK, mockedNavigationState);

    assertEquals(Event.Type.NAV_FEEDBACK, feedbackEvent.obtainType());
  }

  @Test
  public void checksNavigationFasterRouteEvent() throws Exception {
    Context mockedContext = mock(Context.class);
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState aValidNavigationState = obtainAValidNavigationState();
    NavigationRerouteData mockedNavigationRerouteData = mock(NavigationRerouteData.class);
    aValidNavigationState.setNavigationRerouteData(mockedNavigationRerouteData);


    Event fasterRouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FASTER_ROUTE,
      aValidNavigationState);

    assertTrue(fasterRouteEvent instanceof NavigationFasterRouteEvent);
  }

  @Test
  public void checksFasterRouteType() throws Exception {
    Context mockedContext = mock(Context.class);
    MapboxTelemetry.applicationContext = mockedContext;
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
    AudioManager mockedAudioManager = mock(AudioManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mockedAudioManager);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState aValidNavigationState = obtainAValidNavigationState();
    NavigationRerouteData mockedNavigationRerouteData = mock(NavigationRerouteData.class);
    aValidNavigationState.setNavigationRerouteData(mockedNavigationRerouteData);

    Event fasterRouteEvent = navigationEventFactory.createNavigationEvent(Event.Type.NAV_FASTER_ROUTE,
      aValidNavigationState);

    assertEquals(Event.Type.NAV_FASTER_ROUTE, fasterRouteEvent.obtainType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksFasterRouteTypeWithoutNavigationRerouteData() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState invalidFasterRouteNavigationState = mock(NavigationState.class);

    navigationEventFactory.createNavigationEvent(Event.Type.NAV_FASTER_ROUTE, invalidFasterRouteNavigationState);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidType() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    NavigationState mockedNavigationState = mock(NavigationState.class);
    Event.Type notANavigationType = Event.Type.LOCATION;

    navigationEventFactory.createNavigationEvent(notANavigationType, mockedNavigationState);
  }

  @Test
  public void checksValidNavigationState() throws Exception {
    Context mockedContext = mock(Context.class);
    ActivityManager mockedActivityManager = mock(ActivityManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Event.Type aNavFeedbackEventType = Event.Type.NAV_FEEDBACK;
    NavigationState aValidNavigationState = obtainAValidNavigationState();

    Event aNavFeedbackEvent = navigationEventFactory.createNavigationEvent(aNavFeedbackEventType,
      aValidNavigationState);

    assertTrue(aNavFeedbackEvent instanceof NavigationFeedbackEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checksInvalidNavigationState() throws Exception {
    NavigationEventFactory navigationEventFactory = new NavigationEventFactory();
    Event.Type aNavArriveEventType = Event.Type.NAV_ARRIVE;
    NavigationState nullNavigationState = null;

    navigationEventFactory.createNavigationEvent(aNavArriveEventType, nullNavigationState);
  }

  private NavigationState obtainAValidNavigationState() {
    NavigationMetadata metadata = new NavigationMetadata(new Date(), 13, 22, 180, "sdkIdentifier", "sdkVersion",
      3, "sessionID", 10.5, 15.67, "geometry", "profile", false,  "AndroidLocationEngine", 50,
      "tripIdentifier", 3, 5, 2, 3, 10);
    return new NavigationState(metadata);
  }
}