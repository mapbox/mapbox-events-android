package com.mapbox.android.telemetry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigurationClientInstrumentationTest {
  private ConfigurationClient configurationClient;
  private static final long DAY_IN_MILLIS = 86400000;
  private ConfigurationCallback configurationCallback;

  @Before
  public void setup() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    this.configurationCallback = mock(ConfigurationCallback.class);
    this.configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient(), null);
  }

  @Test
  public void checkStateChanged() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient(),
      mock(ConfigurationCallback.class));
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.OVERRIDE);

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, null, null);
    assertTrue(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.CONFIG_DISABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertTrue(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.OVERRIDE);

    configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.ENABLED);
  }

  @Test
  public void checkStateNotChanged() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);

    configuration = new Configuration(new String[]{}, 1, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(telemetryEnabler.obtainTelemetryState() == TelemetryEnabler.State.DISABLED);
  }

  @Test
  public void validateEventsStateAfterConfigChange() {
    TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.ENABLED);
    TelemetryEnabler telemetryEnabler = new TelemetryEnabler(true);
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    ConfigurationClient configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", new OkHttpClient(),
      mock(ConfigurationCallback.class));
    Configuration configuration = new Configuration(new String[]{}, null, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(TelemetryEnabler.isEventsEnabled(telemetryEnabler));

    configuration = new Configuration(new String[]{}, 0, null);
    assertFalse(configurationClient.updateTelemetryState(configuration));
    assertTrue(TelemetryEnabler.isEventsEnabled(telemetryEnabler));

    configuration = new Configuration(new String[]{}, 1, null);
    assertTrue(configurationClient.updateTelemetryState(configuration));
    assertFalse(TelemetryEnabler.isEventsEnabled(telemetryEnabler));
  }

  @Test
  public void checkComEndpoint() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    OkHttpClient httpClient = mock(OkHttpClient.class);
    when(httpClient.newCall(any(Request.class))).thenReturn(mock(Call.class));
    this.configurationClient = new ConfigurationClient(context,
      TelemetryUtils.createFullUserAgent("AnUserAgent", context), "anAccessToken", httpClient, configurationCallback);
    configurationClient.update();
    ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
    verify(httpClient).newCall(argument.capture());
    assertEquals("api.mapbox.com", argument.getValue().url().host());
  }

  @Test
  public void checkCnEndpoint() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
    Bundle bundle = new Bundle();
    bundle.putBoolean("com.mapbox.CnEventsServer", true);
    applicationInfo.metaData = bundle;

    when(context.getPackageName()).thenReturn("package");
    when(packageManager.getApplicationInfo(anyString(), eq(PackageManager.GET_META_DATA))).thenReturn(applicationInfo);
    when(context.getPackageManager()).thenReturn(packageManager);

    OkHttpClient httpClient = mock(OkHttpClient.class);
    when(httpClient.newCall((Request) any())).thenReturn(mock(Call.class));
    this.configurationClient = new ConfigurationClient(context, "AnUserAgent", "anAccessToken",
      httpClient, configurationCallback);

    configurationClient.update();
    ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
    verify(httpClient).newCall(argument.capture());
    assertEquals("api.mapbox.cn", argument.getValue().url().host());
  }

  @Test
  public void shouldUpdateTest() {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);
    assertTrue(configurationClient.shouldUpdate());

    setTimeStamp(System.currentTimeMillis());
    assertFalse(configurationClient.shouldUpdate());
  }

  @Test
  public void saveTimeStampTest() {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);
    Call mockedCall = mock(Call.class);
    IOException mockedException = mock(IOException.class);

    configurationClient.onFailure(mockedCall, mockedException);

    assertFalse(configurationClient.shouldUpdate());
  }

  @Test
  public void updateRequestTest() throws IOException {
    setTimeStamp(System.currentTimeMillis() - DAY_IN_MILLIS);

    ConfigurationChangeHandler configurationChangeHandler = mock(ConfigurationChangeHandler.class);

    configurationClient.addHandler(configurationChangeHandler);
    ResponseBody mockResponseBody = body("{\"crl\":[\"test1\"]}");

    configurationClient.onResponse(mock(Call.class), newResponse(mockResponseBody));
    assertFalse(configurationClient.shouldUpdate());

    verify(configurationChangeHandler, times(1)).onUpdate();
  }

  private void setTimeStamp(long milliseconds) {
    SharedPreferences sharedPreferences =
      TelemetryUtils.obtainSharedPreferences(InstrumentationRegistry.getInstrumentation().getTargetContext());
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong("mapboxConfigSyncTimestamp", milliseconds);
    editor.apply();
  }

  private Response newResponse(ResponseBody responseBody) {
    return new Response.Builder()
      .request(new Request.Builder()
        .url("https://example.com/")
        .build())
      .protocol(Protocol.HTTP_1_1)
      .code(200)
      .message("OK")
      .body(responseBody)
      .build();
  }

  static ResponseBody body(String hex) {
    return body(hex, null);
  }

  static ResponseBody body(String hex, String charset) {
    MediaType mediaType = charset == null ? null : MediaType.parse("any/thing; charset=" + charset);
    return ResponseBody.create(mediaType, hex);
  }
}
