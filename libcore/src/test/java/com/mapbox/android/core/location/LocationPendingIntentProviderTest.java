package com.mapbox.android.core.location;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LocationPendingIntentProviderTest {

  @Test
  public void whenApiVersionIsOreoOrAbove_providerReturnsBroadcastPendingIntent() {
    LocationPendingIntentProvider provider = buildIntentProvider(true);

    LocationPendingIntent pendingIntent = provider.intent();

    assertTrue(pendingIntent instanceof LocationBroadcastPendingIntent);
  }

  @Test
  public void whenApiVersionIsBelowOreo_providerReturnsServicePendingIntent() {
    LocationPendingIntentProvider provider = buildIntentProvider(false);

    LocationPendingIntent pendingIntent = provider.intent();

    assertTrue(pendingIntent instanceof LocationServicePendingIntent);
  }

  @NonNull
  private LocationPendingIntentProvider buildIntentProvider(boolean isOreoOrAbove) {
    setApiLevel(isOreoOrAbove);

    Context context = mock(Context.class);
    CopyOnWriteArrayList<LocationEngineListener> locationEngineListeners = new CopyOnWriteArrayList<>();
    return new LocationPendingIntentProvider(context, locationEngineListeners);
  }

  private void setApiLevel(boolean isOreoOrAbove) {
    int sdkInt = 25;

    if (isOreoOrAbove) {
      sdkInt = 27;
    }

    try {
      setFinalStatic(Build.VERSION.class.getField("SDK_INT"), sdkInt);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }
}