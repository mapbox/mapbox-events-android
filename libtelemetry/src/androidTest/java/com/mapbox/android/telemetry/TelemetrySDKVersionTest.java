package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.test.platform.app.InstrumentationRegistry;
import android.util.Log;

import com.mapbox.android.core.FileUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.fail;

public class TelemetrySDKVersionTest {

  private static final String SECOND_LINE_FORMAT = "v%d";
  private static final String SDK_VERSIONS_FOLDER = "sdk_versions";
  private static final String LOG_TAG = "TelemetrySDKVersionTest";

  @Test
  public void testPersistedTelemetrySDKInfo() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    AssetManager assetManager = context.getAssets();
    InputStream inputStream = null;

    try {
      String packageName = context.getPackageName().replace(".test", "");
      inputStream = assetManager.open(SDK_VERSIONS_FOLDER + File.separator + packageName);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      Assert.assertEquals(reader.readLine().split("/")[1], BuildConfig.VERSION_NAME);
      Assert.assertEquals(reader.readLine(), String.format(SECOND_LINE_FORMAT, BuildConfig.VERSION_CODE));
    } catch (IOException exception) {
      Log.e(LOG_TAG, exception.toString());
      fail(exception.toString());
    } finally {
      FileUtils.closeQuietly(inputStream);
    }
  }
}