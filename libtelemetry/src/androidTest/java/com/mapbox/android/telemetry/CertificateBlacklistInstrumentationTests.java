package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_CONFIGURATION;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.MAPBOX_SHARED_PREFERENCES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class CertificateBlacklistInstrumentationTests {
  private static final String BLACKLIST_FILE = "MapboxBlacklist";

  private CertificateBlacklist certificateBlacklist;

  private SharedPreferences sharedPreferences;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    sharedPreferences = context.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    context.deleteFile("MapboxBlacklist");
    setSystemPrefs();

    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient(), null);
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
    String key = "test12345";
    String initialConfiguration = "{\"crl\" : [\"test12345\"]}";
    updateConfiguration(initialConfiguration);
    assertTrue(certificateBlacklist.isBlacklisted(key));

    String updatedConfiguration = "{\"crl\" : [\"\"]}";
    updateConfiguration(updatedConfiguration);
    assertFalse(certificateBlacklist.isBlacklisted(key));
  }

  private void updateConfiguration(String data) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(MAPBOX_CONFIGURATION, data);
    editor.apply();
    certificateBlacklist.retrieveBlackListForTest(true);
  }

  @Test
  public void checkAttemptCleanUpWithFile() throws IOException {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    String initialConfiguration = "{\"crl\" : [\"test12345\"]}";
    FileOutputStream outputStream = context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE);
    outputStream.write(initialConfiguration.getBytes());
    outputStream.close();
    assertTrue(certificateBlacklist.attemptCleanUp());
  }

  @Test
  public void checkAttemptCleanUpWithoutFile() {
    assertFalse(certificateBlacklist.attemptCleanUp());
  }

  @Test
  public void checkMultiBlackList() {
    String configuration = "{\"crl\" : [\"test1\",\"test2\",\"test3\"]}";
    updateConfiguration(configuration);

    assertTrue(certificateBlacklist.isBlacklisted("test1"));
    assertTrue(certificateBlacklist.isBlacklisted("test2"));
    assertTrue(certificateBlacklist.isBlacklisted("test3"));
  }

  @Test
  public void checkOverwrite() {
    String fileContent = "{\"crl\" : [\"test1\"]}";
    updateConfiguration(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test1"));

    fileContent = "{\"crl\" : [\"test2\"]}";
    updateConfiguration(fileContent);
    assertFalse(certificateBlacklist.isBlacklisted("test1"));
    assertTrue(certificateBlacklist.isBlacklisted("test2"));
  }

  @Test
  public void checkBlacklistSaved() {
    String fileContent = "{\"crl\" : [\"test12345\"]}";
    updateConfiguration(fileContent);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient(), null);
    this.certificateBlacklist = new CertificateBlacklist(context, configurationClient);
    certificateBlacklist.retrieveBlackListForTest(true);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));
  }

  @Test
  public void checkEmptyBlacklist() {
    String configuration = "{\"crl\" : [\"test12345\"]}";
    updateConfiguration(configuration);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    configuration = "";
    updateConfiguration(configuration);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    configuration = "{\"crl\" : []}";
    updateConfiguration(configuration);
    assertTrue(certificateBlacklist.isBlacklisted("test12345"));

    configuration = "{\"crl\" : [\"test1\"]}";
    updateConfiguration(configuration);
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