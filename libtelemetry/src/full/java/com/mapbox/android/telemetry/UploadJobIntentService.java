package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.gson.Gson;
import com.mapbox.libupload.MapboxUploader;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UploadJobIntentService extends JobIntentService implements Callback {
  private static final String LOG_TAG = "UploadJobIntentService";
  private static final int JOB_ID = 564321;
  private static MapboxUploader.MapboxUploadClient<List<Event>, UploadJobIntentService> uploadClient;
  private Gson gson = new Gson();

  public void schedule(Context context, List<Event> events, MapboxUploader.MapboxUploadClient mapboxUploadClient) {
    uploadClient = mapboxUploadClient;

    Intent intent = new Intent("UploadJob");
    intent.putExtra("list", gson.toJson(events));
//    intent.putExtra("client", gson.toJson(mapboxUploadClient));

    enqueueWork(context, UploadJobIntentService.class, JOB_ID, intent);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    Log.e("test", "onHandleWork");
    List<Event> events = gson.fromJson(intent.getStringExtra("list"), List.class);
//    uploadClient = gson.fromJson(intent.getStringExtra("client"), MapboxUploader.MapboxUploadClient.class);

    Log.e("test", "events: " + events);
    uploadClient.upload(events,this);
  }

  @Override
  public void onFailure(Call call, IOException e) {
    Log.e("test", "job - failure: " + e);
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    Log.e("test", "job - response: " + response);
  }
}
