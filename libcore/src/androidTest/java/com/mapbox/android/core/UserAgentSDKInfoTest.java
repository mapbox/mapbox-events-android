package com.mapbox.android.core;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class UserAgentSDKInfoTest {

  private static final String SDK_UA_FORMAT = "%s/%s (%s; %s)";
  private static final String SDK_UA_VERSION_CODE_FORMAT = "v%d";
  private static final Locale LOCALE_DEFAULT = Locale.US;
  private static final String NAME = "libcore";

  @Test
  public void testSDKInformation() {
    Context context = InstrumentationRegistry.getContext();
    String packageName = context.getPackageName().replace(".test", "");
    String versionCode = String.format(LOCALE_DEFAULT, SDK_UA_VERSION_CODE_FORMAT, BuildConfig.VERSION_CODE);
    String sdkInfo = MapboxSdkInfoForUserAgentGenerator.getInstance(context.getAssets())
      .getMapboxSdkIdentifiersForUserAgent(context.getAssets());
    Assert.assertEquals(String.format(Locale.US, SDK_UA_FORMAT, NAME, BuildConfig.VERSION_NAME,
      packageName, versionCode), sdkInfo);
  }

  @Test
  public void testUserAgentSdkInfo() {
    Context context = InstrumentationRegistry.getContext();
    String sdkInfo = MapboxSdkInfoForUserAgentGenerator.getInstance(context.getAssets())
      .getSdkInfoForUserAgent();
    String packageName = context.getPackageName().replace(".test", "");
    String versionCode = String.format(LOCALE_DEFAULT, SDK_UA_VERSION_CODE_FORMAT, BuildConfig.VERSION_CODE);
    Assert.assertEquals(String.format(Locale.US, SDK_UA_FORMAT, NAME, BuildConfig.VERSION_NAME,
      packageName, versionCode), sdkInfo);
  }

  @Test(expected = NullPointerException.class)
  public void testSDKInformationInUserAgentWithNullContext() {
    MapboxSdkInfoForUserAgentGenerator.getInstance(null)
      .getSdkInfoForUserAgent();

  }
}
