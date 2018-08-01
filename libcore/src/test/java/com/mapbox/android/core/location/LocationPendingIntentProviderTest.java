package com.mapbox.android.core.location;

import android.content.Context;
import android.support.annotation.NonNull;

import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationPendingIntentProviderTest {

  @Test
  public void whenApiVersionIsOreoOrAbove_providerReturnsBroadcastPendingIntent() {
    LocationPendingIntentProvider provider = buildIntentProvider(true);

    LocationPendingIntent pendingIntent = provider.buildLocationPendingIntent();

    assertTrue(pendingIntent instanceof LocationBroadcastPendingIntent);
  }

  @Test
  public void whenApiVersionIsBelowOreo_providerReturnsServicePendingIntent() {
    LocationPendingIntentProvider provider = buildIntentProvider(false);

    LocationPendingIntent pendingIntent = provider.buildLocationPendingIntent();

    assertTrue(pendingIntent instanceof LocationServicePendingIntent);
  }

  @NonNull
  private LocationPendingIntentProvider buildIntentProvider(boolean isOreoOrAbove) {
    SdkChecker checker = mock(SdkChecker.class);
    when(checker.isOreoOrAbove()).thenReturn(isOreoOrAbove);
    Context context = mock(Context.class);
    CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners = new CopyOnWriteArrayList<>();
    return new LocationPendingIntentProvider(context, checker, locationEngineListeners);
  }
}