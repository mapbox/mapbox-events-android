package com.mapbox.android.core.location;

import android.support.annotation.Nullable;

final class Utils {
  private Utils() {
    // Prevent instantiation
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference object reference.
   * @param message   exception message to use if check fails.
   * @param <T>       object type.
   * @return validated non-null reference.
   */
  static <T> T checkNotNull(@Nullable T reference, String message) {
    if (reference == null) {
      throw new NullPointerException(message);
    }
    return reference;
  }
}
