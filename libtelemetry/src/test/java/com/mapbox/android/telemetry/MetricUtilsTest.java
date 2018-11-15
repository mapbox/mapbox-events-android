package com.mapbox.android.telemetry;

import android.location.Location;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MetricUtilsTest {

  @Test
  public void checksNewDateGenerated() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    assertNull(metricUtils.getUtcDate());
    assertFalse(metricUtils.isNewDate());
    assertNotNull(metricUtils.getUtcDate());
  }

  @Test
  public void checksDateString() throws Exception {
    MetricUtils metricUtils = new MetricUtils();
    metricUtils.isNewDate();

    Date date = metricUtils.getUtcDate();
    SimpleDateFormat format = new SimpleDateFormat("YYYY-MMM-DD");
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    String dateString = format.format(date);

    assertEquals(dateString, metricUtils.getDateString());
  }

  @Test
  public void checksBuildMetricEvent() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    assertEquals(MetricEvent.class, metricUtils.buildMetricEvent().getClass());
  }

  @Test
  public void checksMetricEventLocationNull() throws Exception {
    MetricUtils metricUtils = new MetricUtils();
    MetricUtils.setLatestLocation(null);
    MetricEvent metricEvent = metricUtils.buildMetricEvent();

    assertNull(metricEvent.getDeviceLat());
    assertNull(metricEvent.getDeviceLon());
  }

  @Test
  public void checksMetricEventLocationNotNull() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    Location testLocation = new Location("test");
    MetricUtils.setLatestLocation(testLocation);
    MetricEvent metricEvent = metricUtils.buildMetricEvent();

    assertNotNull(metricEvent.getDeviceLat());
    assertNotNull(metricEvent.getDeviceLon());
  }

  @Test
  public void checksUpdateFailedRequests() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    assertNull(metricUtils.getFailedRequests());
    metricUtils.updateFailedRequests(400);
    assertNotNull(metricUtils.getFailedRequests());
  }

  @Test
  public void checksIncrementAppWakeups() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    assertEquals(0, metricUtils.getAppWakeups());
    MetricUtils.incrementAppWakeups();
    assertEquals(1, metricUtils.getAppWakeups());
  }

  @Test
  public void checksTimeDiffCalculation() throws Exception {
    MetricUtils metricUtils = new MetricUtils();

    assertEquals(0, metricUtils.getTimeDrift());
    MetricUtils.calculateTimeDiff(10000);
    assertNotEquals(0, metricUtils.getTimeDrift());
  }

  @Test
  public void checksCalculateFailedRequests() throws Exception {
    MetricUtils metricUtils = new MetricUtils();
    Map map = generateMap();
    Map failedRequests = metricUtils.calculateFailedRequests(400, map);

    assertEquals(1, failedRequests.get("400"));
  }

  @Test
  public void checksConvertMapToJson() throws Exception {
    MetricUtils metricUtils = new MetricUtils();
    Map map = generateMap();

    assertEquals("{\"test\":1}", metricUtils.convertMapToJson(map));
  }

  private Map<String, Integer> generateMap() {
    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("test", 1);

    return map;
  }

}
