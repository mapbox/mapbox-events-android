package com.mapbox.libupload;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class UploadJobIntentService extends JobIntentService {
  private static final String LOG_TAG = "UploadJobIntentService";
  private static final int JOB_ID = 564321;

  public void schedule(Context context) {
    Intent intent = new Intent("UploadJob");

    enqueueWork(context, UploadJobIntentService.class, JOB_ID, intent);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {

  }
}
