package com.mapbox.services.android.telemetry.audio;

public class AudioTypeChain {

  public AudioTypeResolver setup() {
    AudioTypeResolver unknown = new UnknownAudioType();
    AudioTypeResolver speaker = new SpeakerAudioType();
    speaker.nextChain(unknown);
    AudioTypeResolver headphones = new HeadphonesAudioType();
    headphones.nextChain(speaker);
    AudioTypeResolver rootOfTheChain = new BluetoothAudioType();
    rootOfTheChain.nextChain(headphones);

    return rootOfTheChain;
  }
}
