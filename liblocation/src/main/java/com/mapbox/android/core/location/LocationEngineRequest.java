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

    private final int interval;
    private final int priority;
    private final float displacement;
    private final boolean isBackground;

    private LocationEngineRequest(Builder builder){
        this.interval = builder.interval;
        this.priority = builder.priority;
        this.displacement = builder.displacement;
        this.isBackground = builder.isBackground;
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
     * Returns true if location updates
     * should be provided in background.
     *
     * @return true if background mode is supported.
     * @since 3.0.0
     */
    public boolean isBackground() {
        return isBackground;
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

    public static final class Builder {
        private final int interval;

        private int priority;
        private float displacement;
        private boolean isBackground;

        /**
         * Default builder constructor.
         *
         * @param interval default interval between location updates
         * @since 3.0.0
         */
        public Builder(int interval) {
            this.interval = interval;
            this.priority = PRIORITY_HIGH_ACCURACY;
            this.displacement = 0f;
            this.isBackground = false;
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
         * Enable background mode for location updates.
         *
         * @param isBackground enable background location.
         * @return reference to builder
         * @since 3.0.0
         */
        public Builder background(boolean isBackground) {
            this.isBackground = isBackground;
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

