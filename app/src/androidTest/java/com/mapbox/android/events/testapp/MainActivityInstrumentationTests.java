package com.mapbox.android.events.testapp;

import android.app.Activity;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentationTests {
  @Rule
  public ActivityTestRule rule = new ActivityTestRule(MainActivity.class, true, true);

  @Test
  public void checkActivity() {
    Activity activity = rule.getActivity();
    assertNotNull(activity);
  }
}
