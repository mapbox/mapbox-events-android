package com.mapbox.android.core.location;

/**
 * Data model that contains parameters for location
 * engine requests.
 *
 * @since 3.0.0
 */
public class LocationEngineRequest {
  /**
   * Used with {@link LocationEngineRequest.Builder#setPriority(int)} to request
   * the most accurate location.
   *
   * @since 3.0.0
   */
  public static final int PRIORITY_HIGH_ACCURACY = 0;

  /**
   * Used with {@link LocationEngineRequest.Builder#setPriority(int)} to request
   * coarse location that is battery optimized.
   *
   * @since 3.0.0
   */
  public static final int PRIORITY_BALANCED_POWER_ACCURACY = 1;

  /**
   * Used with {@link LocationEngineRequest.Builder#setPriority(int)} to request
   * coarse ~ 10 km accuracy location.
   *
   * @since 3.0.0
   */
  public static final int PRIORITY_LOW_POWER = 2;

  /**
   * Used with {@link LocationEngineRequest.Builder#setPriority(int)} to request
   * passive location: no locations will be returned unless a different client
   * has requested location updates.
   *
   * @since 3.0.0
   */
  public static final int PRIORITY_NO_POWER = 3;

  private final long interval;
  private final int priority;
  private final float displacement;
  private final long maxWaitTime;

  private LocationEngineRequest(Builder builder) {
    this.interval = builder.interval;
    this.priority = builder.priority;
    this.displacement = builder.displacement;
    this.maxWaitTime = builder.maxWaitTime;
  }

  /**
   * Returns desired interval between location updates
   * in milliseconds.
   *
   * @return desired interval in milliseconds.
   * @since 3.0.0
   */
  public long getInterval() {
    return interval;
  }

  /**
   * Returns desired quality of the request.
   *
   * @return accuracy constant.
   * @since 3.0.0
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Returns distance between location updates.
   *
   * @return distance between location updates in meters.
   * @since 3.0.0
   */
  public float getDisplacemnt() {
    return displacement;
  }

  /**
   * Returns maximum wait time in milliseconds for location updates.
   *
   * @return maximum wait time in milliseconds.
   * @since 3.0.0
   */
  public long getMaxWaitTime() {
    return maxWaitTime;
  }

  public static final class Builder {
    private final long interval;

    private int priority;
    private float displacement;
    private long maxWaitTime;

    /**
     * Default builder constructor.
     *
     * @param interval default interval between location updates
     * @since 3.0.0
     */
    public Builder(long interval) {
      this.interval = interval;
      this.priority = PRIORITY_HIGH_ACCURACY;
      this.displacement = 3.0f;
      this.maxWaitTime = 0L;
    }

    /**
     * Set priority for request.
     * Use priority constant: {@link #PRIORITY_HIGH_ACCURACY}
     *
     * @param priority constant
     * @return reference to builder
     * @since 3.0.0
     */
    public Builder setPriority(int priority) {
      this.priority = priority;
      return this;
    }

    /**
     * Set distance between location updates.
     *
     * @param displacement distance between locations in meters.
     * @return reference to builder
     * @since 3.0.0
     */
    public Builder setDisplacement(float displacement) {
      this.displacement = displacement;
      return this;
    }

    /**
     * Sets the maximum wait time in milliseconds for location updates.
     * <p>
     * Locations determined at intervals but delivered in batch based on
     * wait time. Batching is not supported by all engines.
     *
     * @param maxWaitTime wait time in milliseconds.
     * @return reference to builder
     * @since 3.0.0
     */
    public Builder setMaxWaitTime(long maxWaitTime) {
      this.maxWaitTime = maxWaitTime;
      return this;
    }

    /**
     * Builds request object.
     *
     * @return instance of location request.
     * @since 3.0.0
     */
    public LocationEngineRequest build() {
      return new LocationEngineRequest(this);
    }
  }
}

