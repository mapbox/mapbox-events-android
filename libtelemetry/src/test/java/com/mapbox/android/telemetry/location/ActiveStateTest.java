package com.mapbox.android.telemetry.location;

import android.location.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStateTest extends BaseStateTest {

  @Test
  public void onResumeResultsInActiveState() {
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveState.class);
  }

  @Test
  public void onAccurateLocationUpdatedInActiveState() {
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(getAccurateLocation()));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveGeofenceState.class);
  }

  @Test
  public void onRoughLocationUpdatedInActiveState() {
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(getRoughLocation()));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveState.class);
  }

  @Test
  public void onTimerExpireddInActiveState() {
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveState.class);
  }

  @Test
  public void ensureBackgroundForegroundTransitionActiveState() {
    State stateBefore = locationEngineController.getCurrentState();
    locationEngineController.onPause();
    locationEngineController.onResume();
    State stateAfter = locationEngineController.getCurrentState();
    assertThat(stateBefore).isEqualTo(stateAfter);
  }

  @Test
  public void onStoppedEventInActiveState() {
    locationEngineController.onDestroy();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(IdleState.class);
  }

  @Test
  public void onLocationUpdatedAfterTimerExpiredInActiveState() {
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(mock(Location.class)));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveState.class);
  }

  @Test
  public void ensureSkipStateTransitionInActiveState() {
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(mock(Location.class)));
    verify(locationEngine, never()).requestLocationUpdates(null, null, null);
  }
}
