package com.mapbox.android.core.location;

/**
 * Callback passed into the contructor of {@link GpxAnimator}.
 * <p>
 * This callback will be triggered once the GPX parsing of the {@link java.io.InputStream} has finished.
 * <p>
 * At this point, it is safe to call {@link GpxAnimator#start()}, beginning location updates.
 *
 * @since 0.3.0
 */
public interface OnGpxAnimatorReadyCallback {

  void onGpxAnimatorReady(GpxAnimator gpxAnimator);
}