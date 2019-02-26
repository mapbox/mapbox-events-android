package com.mapbox.android.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

class Uploader implements MapboxUploader, Callback {
  private MapboxUploadClient<Object, Uploader> mapboxUploadClient;
  private List<Listener> listeners;
  private Context context;
  private Configuration config;

  Uploader(MapboxUploadClient<Object, Uploader> mapboxUploadClient, Context context) {
    this.mapboxUploadClient = mapboxUploadClient;
    this.context = context;
  }

  @Override
  public void send(Object data) {
    if (config != null && config.getUploadInterval() > 0) {
      scheduleJob(data);
    } else {
      mapboxUploadClient.upload(data, this);
    }
  }

  @Override
  public void setConfiguration(Configuration configuration) {
    config = configuration;
  }

  @Override
  public void addListener(Listener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }

    listeners.add(listener);
  }

  @Override
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  private void scheduleJob(Object data) {
    registerJobReceiver();

    UploadJobIntentService uploadJobIntentService = new UploadJobIntentService();
    List<Event> events = (List<Event>) data;

    uploadJobIntentService.schedule(context, events, mapboxUploadClient);
  }

  @Override
  public void onFailure(Call call, IOException exception) {
    Log.e("test", "uploader - failure: " + exception);

    if (listeners != null) {
      for (Listener listener: listeners) {
        listener.onFailure(exception);
      }
    }
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("test", "uploader - response: " + response);

    if (listeners != null) {
      for (Listener listener: listeners) {
        listener.onSuccess(response);
      }
    }
  }

  private void registerJobReceiver() {
    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        if (listeners != null) {
          for (Listener listener: listeners) {
            listener.onSuccess(intent.getStringExtra("response"));
          }
        }
      }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter("jobSuccess"));
  }
}
