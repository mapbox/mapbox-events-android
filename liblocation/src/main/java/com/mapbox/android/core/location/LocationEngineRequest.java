package com.mapbox.android.core.location;

public class LocationEngineRequest {
    public static final int PRIORITY_HIGH_ACCURACY = 0;
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 1;
    public static final int PRIORITY_LOW_POWER = 2;
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

    public int getInterval() {
        return interval;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public float getDisplacemnt() {
        return displacement;
    }

    public static final class Builder {
        private final int interval;

        private int priority;
        private float displacement;
        private boolean isBackground;

        public Builder(int interval) {
            this.interval = interval;
            this.priority = PRIORITY_HIGH_ACCURACY;
            this.displacement = 0f;
            this.isBackground = false;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setDisplacement(float displacement) {
            this.displacement = displacement;
            return this;
        }

        public Builder background(boolean isBackground) {
            this.isBackground = isBackground;
            return this;
        }

        public LocationEngineRequest build() {
            return new LocationEngineRequest(this);
        }
    }
}

