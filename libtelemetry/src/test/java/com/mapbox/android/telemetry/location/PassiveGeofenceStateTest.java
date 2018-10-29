package com.mapbox.android.telemetry.location;

import android.location.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PassiveGeofenceStateTest extends BaseStateTest {
  @Override
  @Before
  public void setUp() {
    super.setUp();
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(getAccurateLocation()));
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
  }

  @Test
  public void onLocationUpdatedInPassiveGeofenceState() {
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(getMovingLocation()));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveGeofenceState.class);
  }

  @Test
  public void onStationaryLocationUpdatedInActiveGeofenceState() {
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(getStationaryLocation()));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveGeofenceState.class);
  }

  @Test
  public void ensureBackgroundForegroundTransitionPassiveGeofenceState() {
    State stateBefore = locationEngineController.getCurrentState();
    locationEngineController.onPause();
    locationEngineController.onResume();
    State stateAfter = locationEngineController.getCurrentState();
    assertThat(stateBefore).isEqualTo(stateAfter);
  }

  @Test
  public void onGeofenceExitedPassiveGeofenceState() {
    locationEngineController.handleEvent(EventFactory.createGeofenceExiteEvent(getAccurateLocation()));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveState.class);
  }

  @Test
  public void onBackgroundPassiveGeofenceState() {
    locationEngineController.onPause();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveState.class);
  }

  @Test
  public void onStopPassiveGeofenceState() {
    locationEngineController.onDestroy();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(IdleState.class);
  }

  private static Location getMovingLocation() {
    Location location = mock(Location.class);
    when(location.getSpeed()).thenReturn(20.0f);
    return location;
  }

  private static Location getStationaryLocation() {
    return getLocation(5.0f);
  }
}