package com.mapbox.android.telemetry;

import android.content.Context;

public interface MapboxUploader <T, E> {

  void send(T data);

  void setConfiguration(Configuration configuration);

  void addListener(Listener<E> listener);

  void removeListener(Listener<E> listener);

  interface Listener<F> {
    void onSuccess(F data);
    void onFailure(Exception exception);
  }

  interface Configuration {
    void setUploadInterval(long interval);
    long getUploadInterval();
  }

  interface MapboxUploadClient <B, D> {
    void upload(B data, D callback);
  }
}
