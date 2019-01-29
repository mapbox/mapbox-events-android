package com.mapbox.android.telemetry;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.mapbox.android.telemetry.datarepo.DataSource;
import com.mapbox.android.telemetry.datarepo.EventRepository;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.mapbox.android.telemetry.TelemetryEnabler.MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE;
import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCES;
import static com.mapbox.android.telemetry.TelemetryUtils.MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventRepositoryTest {

  @Test
  public void checkMultiThread() throws InterruptedException {
    final EventRepository repository = EventRepository.getInstance();
    final int num = 100;
    final int threadNum = 5;
    final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
    class PutThread extends Thread {
      @Override
      public void run() {
        for (int i = 0; i < num; i++) {
          MapClickEvent clickEvent = new MapClickEvent(new MapState(0, 0, 0));
          repository.put(clickEvent);
        }
        countDownLatch.countDown();
      }
    }

    for (int i = 0; i < threadNum; i++) {
      new PutThread().start();
    }

    countDownLatch.await();
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertNotNull(data);
        assertEquals(num * threadNum, data.size());
        assertTrue(data.get(0) instanceof MapClickEvent);
        assertTrue(data.get(data.size() - 1) instanceof MapClickEvent);

      }
    });

    MapClickEvent clickEvent = new MapClickEvent(new MapState(0, 0, 0));
    repository.put(clickEvent);
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.get(0) instanceof MapClickEvent);
      }
    });
  }

  @Test
  public void checkEmptyRepo() {
    EventRepository repository = EventRepository.getInstance();

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertNotNull(data);
        assertEquals(0, data.size());
      }
    });
  }

  @Test
  public void checkPutEvents() {
    EventRepository repository = EventRepository.getInstance();
    Event turnstile = obtainAnAppUserTurnstileEvent();
    repository.put(turnstile);
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(1, data.size());
        assertTrue(data.get(0) instanceof AppUserTurnstile);
      }
    });

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(0, data.size());
      }
    });
  }

  @Test
  public void checkPutAttachment() {
    EventRepository repository = EventRepository.getInstance();
    Attachment attachment = new Attachment();
    repository.put(attachment);
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(1, data.size());
        assertTrue(data.get(0) instanceof Attachment);
      }
    });
  }

  @Test
  public void checkMapEvents() {
    EventRepository repository = EventRepository.getInstance();
    MapClickEvent clickEvent = new MapClickEvent(new MapState(0, 0, 0));
    repository.put(clickEvent);
    MapDragendEvent dragendEvent = new MapDragendEvent(new MapState(0, 0.0, 0.0));
    repository.put(dragendEvent);
    MapLoadEvent loadEvent = new MapLoadEvent("");
    repository.put(loadEvent);

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(3, data.size());
        assertTrue(data.get(0) instanceof MapClickEvent);
        assertTrue(data.get(1) instanceof MapDragendEvent);
        assertTrue(data.get(2) instanceof MapLoadEvent);
      }
    });
  }

  @Test
  public void checkVersionEvents() {
    EventRepository repository = EventRepository.getInstance();
    initializeMapboxTelemetry();
    VisionEventFactory visionEventFactory = new VisionEventFactory();
    repository.put(visionEventFactory.createAttachment(Event.Type.VIS_ATTACHMENT));
    repository.put(visionEventFactory.createVisionEvent(Event.Type.VIS_GENERAL));

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(2, data.size());
        assertTrue(data.get(0) instanceof Attachment);
        assertTrue(data.get(1) instanceof VisionEvent);
      }
    });
  }

  @Test
  public void checkOfflineEvents() {
    EventRepository repository = EventRepository.getInstance();
    OfflineDownloadStartEvent startEvent = new OfflineDownloadStartEvent("", 0.0, 0.0);
    repository.put(startEvent);
    OfflineDownloadEndEvent endEvent = new OfflineDownloadEndEvent("", 0.0, 0.0);
    repository.put(endEvent);

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(2, data.size());
        assertTrue(data.get(0) instanceof OfflineDownloadStartEvent);
        assertTrue(data.get(1) instanceof OfflineDownloadEndEvent);
      }
    });
  }

  @Test
  public void checkNavigationEvents() {
    EventRepository repository = EventRepository.getInstance();
    NavigationEventFactory factory = new NavigationEventFactory();
    NavigationState state = mock(NavigationState.class);
    when(state.getNavigationRerouteData()).thenReturn(mock(NavigationRerouteData.class));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_ARRIVE, state));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_CANCEL, state));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_DEPART, state));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_FASTER_ROUTE, state));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_FEEDBACK, state));
    repository.put(factory.createNavigationEvent(Event.Type.NAV_REROUTE, state));

    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(6, data.size());
        assertTrue(data.get(0) instanceof NavigationArriveEvent);
        assertTrue(data.get(1) instanceof NavigationCancelEvent);
        assertTrue(data.get(2) instanceof NavigationDepartEvent);
        assertTrue(data.get(3) instanceof NavigationFasterRouteEvent);
        assertTrue(data.get(4) instanceof NavigationFeedbackEvent);
        assertTrue(data.get(5) instanceof NavigationRerouteEvent);
      }
    });
  }

  @Test
  public void checkPutLotsEvents() {
    EventRepository repository = EventRepository.getInstance();
    final int num = 10000;
    for (int i = 0; i < num; i++) {
      LocationEvent locationEvent = new LocationEvent("sessionId", 0, 0, "");
      repository.put(locationEvent);
    }
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(num, data.size());
        Event event = data.get(0);
        assertTrue(event instanceof LocationEvent);
      }
    });
  }

  @Test
  public void checkClear() {
    EventRepository repository = EventRepository.getInstance();
    final int num = 10;
    for (int i = 0; i < num; i++) {
      LocationEvent locationEvent = new LocationEvent("sessionId", 0, 0, "");
      repository.put(locationEvent);
    }
    repository.clear();
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(0, data.size());
      }
    });
  }

  @Test
  public void checkGetAll() {
    EventRepository repository = EventRepository.getInstance();
    final int num = 10;
    for (int i = 0; i < num; i++) {
      LocationEvent locationEvent = new LocationEvent("sessionId", 0, 0, "");
      repository.put(locationEvent);
    }
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(num, data.size());
        Event event = data.get(0);
        assertTrue(event instanceof LocationEvent);
      }
    });

    for (int i = 0; i < num; i++) {
      LocationEvent locationEvent = new LocationEvent("sessionId", 0, 0, "");
      repository.put(locationEvent);
    }
    repository.getAll(new DataSource.DataSourceCallback() {
      @Override
      public void onDataAvailable(@NonNull List<Event> data) {
        assertEquals(num, data.size());
        Event event = data.get(0);
        assertTrue(event instanceof LocationEvent);
      }
    });
  }

  private Event obtainAnAppUserTurnstileEvent() {
    Context mockedContext = mock(Context.class);
    SharedPreferences mockedSharedPreferences = mock(SharedPreferences.class);
    when(mockedContext.getSharedPreferences(MAPBOX_SHARED_PREFERENCES, Context.MODE_PRIVATE))
      .thenReturn(mockedSharedPreferences);
    when(mockedSharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_TELEMETRY_STATE,
      TelemetryEnabler.State.DISABLED.name())).thenReturn(TelemetryEnabler.State.DISABLED.name());
    when(mockedSharedPreferences.getString(MAPBOX_SHARED_PREFERENCE_KEY_VENDOR_ID, "")).thenReturn("");
    SharedPreferences.Editor mockedEditor = mock(SharedPreferences.Editor.class);
    when(mockedSharedPreferences.edit()).thenReturn(mockedEditor);
    MapboxTelemetry.applicationContext = mockedContext;
    return new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);
  }

  private void initializeMapboxTelemetry() {
    Context mockedContext = mock(Context.class, RETURNS_DEEP_STUBS);
    TelephonyManager mockedTelephonyManager = mock(TelephonyManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockedTelephonyManager);
    WindowManager mockedWindowManager = mock(WindowManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockedWindowManager);
    AlarmManager mockedAlarmManager = mock(AlarmManager.class, RETURNS_DEEP_STUBS);
    when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
    MapboxTelemetry.applicationContext = mockedContext;
  }
}
