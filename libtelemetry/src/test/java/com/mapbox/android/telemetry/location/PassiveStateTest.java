package com.mapbox.android.telemetry.location;

import android.location.Location;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class PassiveStateTest extends BaseStateTest {
  @Mock
  private Location location;

  @Override
  @After
  public void tearDown() {
    super.tearDown();
    reset(location);
  }

  @Test
  public void onPauseResultsInPassiveState() {
    locationEngineController.onPause();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveState.class);
  }

  @Test
  public void onLocationUpdatedPassiveState() {
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(location));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(ActiveState.class);
  }

  @Test
  public void onLocationUpdatedAfterTimerExpiredInBackgroundPassiveState() {
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
    locationEngineController.onPause();
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(location));
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveState.class);
  }

  @Test
  public void onLocationUpdateInBackgroundPassiveState() {
    State stateBefore = locationEngineController.getCurrentState();
    locationEngineController.onPause();
    locationEngineController.handleEvent(EventFactory.createLocationUpdatedEvent(location));
    State stateIntermediate = locationEngineController.getCurrentState();
    assertThat(stateIntermediate).isInstanceOf(PassiveState.class);
    locationEngineController.onResume();
    State stateAfter = locationEngineController.getCurrentState();
    assertThat(stateBefore).isEqualTo(stateAfter);
  }

  @Test
  public void onPauseAndResumePassiveStateAfterTimerEvent() {
    locationEngineController.handleEvent(EventFactory.createTimerExpiredEvent());
    locationEngineController.onPause();
    locationEngineController.onResume();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(PassiveState.class);
  }

  @Test
  public void onStoppedPassiveState() {
    locationEngineController.onDestroy();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(IdleState.class);
  }
}
