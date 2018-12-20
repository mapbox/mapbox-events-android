package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CertificateBlacklistInstrumentationTests {
  private CertificateBlacklist certificateBlacklist;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getTargetContext();
    setSystemPrefs();

    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
  }

  @Test
  public void checkBlacklistSaved() throws Exception {
    List<String> oneItemList = new ArrayList<>();
    oneItemList.add("test12345");
    String fileContent = "{\"RevokedCertKeys\" : [\"test12345\"]}";

    certificateBlacklist.onUpdate(fileContent);

    assertTrue(certificateBlacklist.isBlacklisted(oneItemList.get(0)));
  }

  private void setSystemPrefs() {
    SharedPreferences sharedPreferences =
      TelemetryUtils.obtainSharedPreferences(InstrumentationRegistry.getTargetContext());
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong("mapboxConfigSyncTimestamp", System.currentTimeMillis());
    editor.apply();
  }
}
