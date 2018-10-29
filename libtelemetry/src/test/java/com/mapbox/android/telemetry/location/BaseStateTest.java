package com.mapbox.android.telemetry.location;

import android.location.Location;
import com.mapbox.android.core.location.LocationEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaseStateTest {
  @Mock
  LocationEngine locationEngine;

  @Mock
  private LocationEngineController.Callback callback;

  @Mock
  private Timer timer;

  LocationEngineController locationEngineController;

  @Before
  public void setUp() {
    locationEngineController = new LocationEngineController(locationEngine, timer, callback);
    locationEngineController.onResume();
  }

  @Test
  public void onDestroyResultInIdleState() {
    locationEngineController.onDestroy();
    State state = locationEngineController.getCurrentState();
    assertThat(state).isInstanceOf(IdleState.class);
  }

  @After
  public void tearDown() {
    locationEngineController = null;
  }

  Location getAccurateLocation() {
    return getLocation(5f);
  }

  Location getRoughLocation() {
    return getLocation(500f);
  }

  static Location getLocation(float accuracy) {
    Location location = mock(Location.class);
    when(location.getAccuracy()).thenReturn(accuracy);
    return location;
  }
}