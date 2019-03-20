package com.mapbox.android.telemetry.location;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.mapbox.android.telemetry.MapboxTelemetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static com.mapbox.android.telemetry.MapboxTelemetryConstants.LOCATION_COLLECTOR_ENABLED;
import static com.mapbox.android.telemetry.MapboxTelemetryConstants.SESSION_ROTATION_INTERVAL_MILLIS;
import static com.mapbox.android.telemetry.location.LocationCollectionClient.DEFAULT_SESSION_ROTATION_INTERVAL_HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LocationCollectionClientTest {

  @Mock
  private LocationEngineController locationEngineController;

  @Mock
  private HandlerThread handlerThread;

  @Mock
  private MapboxTelemetry mapboxTelemetry;

  private LocationCollectionClient collectionClient;

  @Before
  public void setUp() {
    collectionClient = new LocationCollectionClient(locationEngineController,
      handlerThread, new SessionIdentifier(TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS)),
      getMockedSharedPrefs(), mapboxTelemetry);
  }

  @After
  public void tearDown() {
    reset(locationEngineController);
    reset(handlerThread);
    reset(mapboxTelemetry);
    collectionClient = null;
  }

  @Test
  public void verifyDefaultStatus() {
    assertThat(collectionClient.isEnabled()).isFalse();
  }

  @Test
  public void verifyDupStatusHasNoEffect() {
    Handler mockHandler = getMockEmptyMessageHandler();
    collectionClient.setMockHandler(mockHandler);
    collectionClient.setEnabled(false);
    verify(locationEngineController, never()).onResume();
  }

  @Test
  public void verifyEnableTriggersOnResume() {
    Handler mockHandler = getMockEmptyMessageHandler();
    collectionClient.setMockHandler(mockHandler);
    collectionClient.setEnabled(true);
    verify(locationEngineController, times(1)).onResume();
  }

  @Test
  public void verifyDisableTriggersOnDestroy() {
    Handler mockHandler = getMockEmptyMessageHandler();
    collectionClient.setMockHandler(mockHandler);
    collectionClient.setEnabled(true);
    collectionClient.setEnabled(false);
    verify(locationEngineController, times(1)).onDestroy();
  }

  @Test
  public void verifyDefaultInterval() {
    assertThat(collectionClient.getSessionRotationInterval())
      .isEqualTo(TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS));
  }

  @Test
  public void verifyCorrectIntevalSet() {
    long interval = 2000L;
    collectionClient.setSessionRotationInterval(interval);
    assertThat(collectionClient.getSessionRotationInterval()).isEqualTo(interval);
  }

  @Test
  public void verifySessionIdUpdatedWithIntervalChange() {
    String sessionId = collectionClient.getSessionId();
    collectionClient.setSessionRotationInterval(2000);
    assertThat(collectionClient.getSessionId()).isNotEqualTo(sessionId);
  }

  @Test
  public void verifySessionIdChangedAfterIntervalExpired() throws InterruptedException {
    long interval = 2000L;
    collectionClient.setSessionRotationInterval(interval);
    String sessionId = collectionClient.getSessionId();
    Thread.sleep(interval);
    assertThat(collectionClient.getSessionId()).isNotEqualTo(sessionId);
  }

  @Test
  public void verifyInvalidKeyChangedViaSharedPrefs() {
    SharedPreferences mockedSharedPrefs = getMockedSharedPrefs();
    collectionClient.onSharedPreferenceChanged(mockedSharedPrefs, "foo");
    verifyZeroInteractions(mockedSharedPrefs);
  }


  @Test
  public void verifyStatusChangeViaSharedPrefs() {
    SharedPreferences mockedSharedPrefs = getMockedSharedPrefs();
    when(mockedSharedPrefs.getBoolean(LOCATION_COLLECTOR_ENABLED, false)).thenReturn(true);
    collectionClient.onSharedPreferenceChanged(mockedSharedPrefs, LOCATION_COLLECTOR_ENABLED);
    assertThat(collectionClient.isEnabled()).isTrue();
  }

  @Test
  public void verifySessionRotationIntervalViaSharedPrefs() {
    long interval = 2000L;
    SharedPreferences mockedSharedPrefs = getMockedSharedPrefs();
    when(mockedSharedPrefs.getLong(SESSION_ROTATION_INTERVAL_MILLIS,
      TimeUnit.HOURS.toMillis(DEFAULT_SESSION_ROTATION_INTERVAL_HOURS))).thenReturn(interval);
    collectionClient.onSharedPreferenceChanged(mockedSharedPrefs, SESSION_ROTATION_INTERVAL_MILLIS);
    assertThat(collectionClient.getSessionRotationInterval()).isEqualTo(interval);
  }

  @Test(expected = IllegalStateException.class)
  public void callGetInstanceBeforeInstall() {
    LocationCollectionClient.getInstance();
  }

  @Test
  public void callUninstallBeforeInstall() {
    assertThat(LocationCollectionClient.uninstall()).isFalse();
  }

  private Handler getMockEmptyMessageHandler() {
    Handler handler = mock(Handler.class);
    when(handler.sendEmptyMessage(anyInt())).thenAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        int what = (int) invocation.getArguments()[0];
        Message message = mock(Message.class);
        message.what = what;
        collectionClient.handleSettingsChangeMessage(message);
        return null;
      }
    });
    return handler;
  }

  private static SharedPreferences getMockedSharedPrefs() {
    SharedPreferences mockedPrefs = mock(SharedPreferences.class);
    when(mockedPrefs.edit()).thenReturn(mock(SharedPreferences.Editor.class));
    return mockedPrefs;
  }
}