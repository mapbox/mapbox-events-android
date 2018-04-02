package com.mapbox.android.telemetry;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;

class NavigationUtils {
  private static double PERCENT_NORMALIZER = 100.0;
  private static double SCREEN_BRIGHTNESS_MAX = 255.0;
  private static int BRIGHTNESS_EXCEPTION_VALUE = -1;

  static int obtainVolumeLevel() {
    AudioManager audioManager = (AudioManager) MapboxTelemetry.applicationContext
      .getSystemService(Context.AUDIO_SERVICE);

    return (int) Math.floor(PERCENT_NORMALIZER * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
      / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
  }

  static int obtainScreenBrightness() {
    int screenBrightness;
    try {
      screenBrightness = android.provider.Settings.System.getInt(
        MapboxTelemetry.applicationContext.getContentResolver(),
        android.provider.Settings.System.SCREEN_BRIGHTNESS);

      screenBrightness = calculateScreenBrightnessPercentage(screenBrightness);
    } catch (Settings.SettingNotFoundException exception) {
      screenBrightness = BRIGHTNESS_EXCEPTION_VALUE;
    }

    return screenBrightness;
  }

  static String obtainAudioType() {
    AudioManager audioManager = (AudioManager) MapboxTelemetry.applicationContext
      .getSystemService(Context.AUDIO_SERVICE);
    AudioTypeChain audioTypeChain = new AudioTypeChain();
    AudioTypeResolver setupChain = audioTypeChain.setup();

    return setupChain.obtainAudioType(MapboxTelemetry.applicationContext, audioManager);
  }

  private static int calculateScreenBrightnessPercentage(int screenBrightness) {
    return (int) Math.floor(PERCENT_NORMALIZER * screenBrightness / SCREEN_BRIGHTNESS_MAX);
  }
}
