package com.mapbox.android.core.location;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationEngineResultTest {

  @Test
  public void checkNullIntent() {
    LocationEngineResult result = LocationEngineResult.extractResult(null);
    assertThat(result).isNull();
  }

  @Test
  public void passInvalidIntent() {
    Intent intent = mock(Intent.class);
    LocationEngineResult result = LocationEngineResult.extractResult(intent);
    assertThat(result).isNull();
  }

  @Test
  public void passValidIntent() {
    Location location = mock(Location.class);
    LocationEngineResult result = LocationEngineResult.extractResult(getValidIntent(location));
    assertThat(result).isNotNull();
    assertThat(result.getLastLocation()).isSameAs(location);
  }

  private static Intent getValidIntent(Location location) {
    Intent intent = mock(Intent.class);
    when(intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)).thenReturn(true);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getParcelable(LocationManager.KEY_LOCATION_CHANGED)).thenReturn(location);
    when(intent.getExtras()).thenReturn(bundle);
    return intent;
  }
}
