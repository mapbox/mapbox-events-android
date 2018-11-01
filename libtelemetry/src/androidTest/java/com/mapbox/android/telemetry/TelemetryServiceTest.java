package com.mapbox.android.telemetry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TelemetryServiceTest {
  @Rule
  public GrantPermissionRule permissionRule =
    GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

  @Rule
  public final ServiceTestRule mServiceRule = new ServiceTestRule();

  @Test
  public void checksLocationReceiverIsUpWhenServiceStarted() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);

    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);

    assertLocationReceiverRegistered(boundService);
  }

  @Test
  public void checksTelemetryReceiverIsUpWhenServiceStarted() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);

    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);

    assertTelemetryReceiverRegistered(boundService);
  }

  @Test
  public void checksLocationReceiverIsDownWhenServiceStopped() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);
    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);

    stopService(serviceIntent, boundService);
    waitUntilServiceIsDestroyed();

    assertLocationReceiverNotRegistered(boundService);
  }

  @Test
  public void checksTelemetryReceiverIsDownWhenServiceStopped() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);
    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);

    stopService(serviceIntent, boundService);
    waitUntilServiceIsDestroyed();

    assertTelemetryReceiverNotRegistered(boundService);
  }

  @Test
  public void checksLocationReceiverIsDownWhenOnBackgroundCalled() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);
    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);

    backgroundService(boundService);

    assertLocationReceiverNotRegistered(boundService);
  }

  @Test
  public void checksTelemetryReceiverIsUpWhenOnForegroundCalled() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);
    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);
    backgroundService(boundService);

    foregroundService(boundService);

    assertLocationReceiverRegistered(boundService);
  }

  @Test
  public void checksOnTaskRemovedCallbackWhenOnTaskRemovedCalled() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);
    final TelemetryService[] boundService = new TelemetryService[1];
    final CountDownLatch latchConnected = new CountDownLatch(1);
    ServiceConnection serviceConnection = setupServiceConnection(boundService, latchConnected);
    startService(serviceIntent);
    waitUntilServiceIsBound(serviceIntent, latchConnected, serviceConnection);
    ServiceTaskCallback mockedCallback = mock(ServiceTaskCallback.class);
    boundService[0].addServiceTask(mockedCallback);

    boundService[0].onTaskRemoved(serviceIntent);

    verify(mockedCallback, times(1)).onTaskRemoved();
  }

  @Test
  public void checksLocationPermission() throws Exception {
    Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TelemetryService.class);
    serviceIntent.putExtra("isLocationEnablerFromPreferences", false);

    IBinder binder = mServiceRule.bindService(serviceIntent);
    TelemetryService service = ((TelemetryService.TelemetryBinder) binder).obtainService();

    assertTrue(service.locationPermissionCheck());
  }

  private ServiceConnection setupServiceConnection(final TelemetryService[] boundService,
                                                   final CountDownLatch latchConnected) {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder binder) {
        boundService[0] = ((TelemetryService.TelemetryBinder) binder).obtainService();
        latchConnected.countDown();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
      }
    };
  }

  private void startService(Intent serviceIntent) {
    InstrumentationRegistry.getTargetContext().startService(serviceIntent);
  }

  private void waitUntilServiceIsBound(Intent serviceIntent, CountDownLatch latchConnected,
                                       ServiceConnection serviceConnection) throws InterruptedException {
    InstrumentationRegistry.getTargetContext().bindService(serviceIntent, serviceConnection, 0);
    latchConnected.await();
  }

  private void assertLocationReceiverRegistered(TelemetryService[] telemetryService) {
    assertTrue(telemetryService[0].isLocationReceiverRegistered());
  }

  private void assertTelemetryReceiverRegistered(TelemetryService[] telemetryService) {
    assertTrue(telemetryService[0].isTelemetryReceiverRegistered());
  }

  private void stopService(Intent serviceIntent, TelemetryService[] telemetryService) {
    telemetryService[0].stopService(serviceIntent);
  }

  private void waitUntilServiceIsDestroyed() throws InterruptedException {
    // Have to wait a bit until the system calls Service#onDestroy()
    // Note that it only needs 15 milliseconds to clean the resources up
    Thread.sleep(15);
  }

  private void assertLocationReceiverNotRegistered(TelemetryService[] telemetryService) {
    assertFalse(telemetryService[0].isLocationReceiverRegistered());
  }

  private void assertTelemetryReceiverNotRegistered(TelemetryService[] telemetryService) {
    assertFalse(telemetryService[0].isTelemetryReceiverRegistered());
  }

  private void backgroundService(TelemetryService[] telemetryService) {
    telemetryService[0].onBackground();
  }

  private void foregroundService(TelemetryService[] telemetryService) {
    telemetryService[0].onForeground();
  }
}