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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;

@RunWith(MockitoJUnitRunner.class)
public class LocationCollectionClientTest {

  @Mock
  private LocationEngineController locationEngineController;

  @Mock
  private HandlerThread handlerThread;

  @Mock
  private SharedPreferences sharedPreferences;

  @Mock
  private MapboxTelemetry mapboxTelemetry;

  private LocationCollectionClient collectionClient;

  @Before
  public void setUp() {
    collectionClient = new LocationCollectionClient(locationEngineController,
      handlerThread, sharedPreferences, mapboxTelemetry);
  }

  @After
  public void tearDown() {
    reset(locationEngineController);
    reset(handlerThread);
    reset(sharedPreferences);
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
}