package com.mapbox.libupload;

import android.content.Context;

public interface MapboxUploader <T, E> {

  public void send(T data);

  public void setConfiguration(Configuration configuration);

  public void addListener(Listener<E> listener);

  public void removeListener(Listener<E> listener);

  interface Listener<F> {
    void onSuccess(F data);
    void onFailure(Exception exception);
  }

  interface Configuration {
    void setUploadInterval(long interval);
    long getUploadInterval();
  }

  interface MapboxUploadClient <B, D> {
    public void upload(B data, D callback);
  }
}
