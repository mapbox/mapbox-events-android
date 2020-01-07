package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class CertificateBlacklistInstrumentationTests {
  private CertificateBlacklist certificateBlacklist;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    context.deleteFile("MapboxBlacklist");
    setSystemPrefs();

    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
  }

  @Test
  public void checkUpdateOnCreate() {
    ConfigurationClient configurationClient = mock(ConfigurationClient.class);
    when(configurationClient.shouldUpdate()).thenReturn(true);
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    verify(configurationClient, times(1)).update();
  }

  @Test
  public void checkBlacklistMalformed() {
    List<String> oneItemList = new ArrayList<>();
    oneItemList.add("test12345");
    String preContent = "{\"RevokedCertKeys\" : [\"\"]}";
    String fileContent = "{\"RevokedCertKeys\" : \"test12345\"}";

    certificateBlacklist.onUpdate(preContent);
    assertFalse(certificateBlacklist.isBlacklisted(oneItemList.get(0)));
    certificateBlacklist.onUpdate(fileContent);
    assertFalse(certificateBlacklist.isBlacklisted(oneItemList.get(0)));
  }

  @Test
  public void checkMultiBlackList() {
    String preContent = "{\"RevokedCertKeys\" : [\"test1\",\"test2\",\"test3\"]}";
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    File file = new File(context.getFilesDir(), "MapboxBlacklist");
    assertFalse(file.exists());

    certificateBlacklist.onUpdate(preContent);
    assertTrue(certificateBlacklist.isBlacklisted("test1"));
    assertTrue(certificateBlacklist.isBlacklisted("test2"));
    assertTrue(certificateBlacklist.isBlacklisted("test3"));

    file = new File(context.getFilesDir(), "MapboxBlacklist");
    assertTrue(file.exists());
  }

  @Test
  public void checkOverwrite() {
    String fileContent = "{\"RevokedCertKeys\" : [\"test1\"]}";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test1"));

    fileContent = "{\"RevokedCertKeys\" : [\"test2\"]}";
    certificateBlacklist.onUpdate(fileContent);
    assertFalse(certificateBlacklist.isBlacklisted("test1"));
    assertTrue(certificateBlacklist.isBlacklisted("test2"));
  }

  @Test
  public void checkBlacklistSaved() {
    String fileContent = "{\"RevokedCertKeys\" : [\"test12345\"]}";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient());
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));
  }

  @Test
  public void checkEmptyBlacklist() {
    String fileContent = "{\"RevokedCertKeys\" : [\"test12345\"]}";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    fileContent = "";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    fileContent = "{\"RevokedCertKeys\" : []}";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    fileContent = "{\"RevokedCertKeys\" : [\"test1\"]}";
    certificateBlacklist.onUpdate(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test1"));
  }

  private void setSystemPrefs() {
    SharedPreferences sharedPreferences =
      TelemetryUtils.obtainSharedPreferences(InstrumentationRegistry.getInstrumentation().getTargetContext());
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong("mapboxConfigSyncTimestamp", System.currentTimeMillis());
    editor.apply();
  }
}