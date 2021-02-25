package com.mapbox.android.core.crashreporter;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class CrashReportInstrumentedTest {
  private final String validJson = "{"
    + "  \"query\": \"Pizza\", "
    + "  \"locations\": [ 94043, 90210 ] "
    + "}";

  private final String invalidJson = "{"
    + "  \"locations\": [ 94043, 90210"
    + "}";

  private final String defaultJsonTemplate = "{"
    + "\"event\": \"mobile.crash\", "
    + "\"created\": \"2021-01-10T12:00:00.000+0200\""
    + "}";

  @Test
  public void validJsonString() {
    try {
      new CrashReport(validJson);
    } catch (JSONException exception) {
      fail("Unexpected JSONException thrown");
    }
  }

  @Test
  public void invalidJsonString() {
    try {
      new CrashReport(invalidJson);
      fail("Expected an JSONException to be thrown");
    } catch (JSONException exception) {
      // noop
    }
  }

  @Test
  public void putNullValue() {
    CrashReport report = new CrashReport(new GregorianCalendar());
    report.put("foo", null);
    assertEquals(report.getString("foo"), "null");
  }

  @Test
  public void checkDateCreated() {
    CrashReport report = new CrashReport(new GregorianCalendar());
    assertFalse(report.getDateString().isEmpty());
  }

  @Test
  public void putJSONArray() throws JSONException {
    CrashReport report = new CrashReport(defaultJsonTemplate);
    Map<String, String> map = new HashMap<>(4);
    map.put("foo", "bar");
    Map<String, String> map2 = new HashMap<>(4);
    map2.put("testKey", "testValue");
    JSONArray jsonArray = new JSONArray(Arrays.asList(map, map2));
    report.put("customData", jsonArray);

    assertEquals(
      ("{"
        + "    \"event\": \"mobile.crash\","
        + "    \"created\": \"2021-01-10T12:00:00.000+0200\","
        + "    \"customData\": ["
        + "        {"
        + "            \"foo\": \"bar\""
        + "        },"
        + "        {"
        + "            \"testKey\": \"testValue\""
        + "        }"
        + "    ]"
        + "}").replace(" ", ""),  // To remove pretty printing from JSON
      report.toJson()
    );
  }
}
