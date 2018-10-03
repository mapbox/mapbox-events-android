package com.mapbox.android.core.location;

import android.support.annotation.NonNull;

public interface LocationEngineCallback<T> {
    void onSuccess(T result);
    void onFailure(@NonNull Exception exception);
}