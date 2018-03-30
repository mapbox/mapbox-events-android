package com.mapbox.android.telemetry;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;

class NavigationUtils {
  static  AudioManager audioManager = null;

  static int getVolumeLevel() {
    if (audioManager == null) {
      audioManager = (AudioManager) MapboxTelemetry.applicationContext.getSystemService(Context.AUDIO_SERVICE);
    }

    return (int) Math.floor(100.0 * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
      / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
  }

  static int getScreenBrightness() {
    int screenBrightness;
    try {
      screenBrightness = android.provider.Settings.System.getInt(
        MapboxTelemetry.applicationContext.getContentResolver(),
        android.provider.Settings.System.SCREEN_BRIGHTNESS);

      // Android returns values between 0 and 255, here we normalize to 0-100.
      screenBrightness = (int) Math.floor(100.0 * screenBrightness / 255.0);
    } catch (Settings.SettingNotFoundException exception) {
      screenBrightness = -1;
    }

    return screenBrightness;
  }

  static String obtainAudioType() {
    AudioTypeChain audioTypeChain = new AudioTypeChain();
    AudioTypeResolver setupChain = audioTypeChain.setup();

    return setupChain.obtainAudioType(MapboxTelemetry.applicationContext, audioManager);
  }
}
