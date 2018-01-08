package com.mapbox.android.telemetry;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationVoiceData implements Parcelable {
  private String voiceInstruction;
  private String voiceInstructionTimestamp;

  public NavigationVoiceData(String voiceInstruction, String voiceInstructionTimestamp) {
    this.voiceInstruction = voiceInstruction;
    this.voiceInstructionTimestamp = voiceInstructionTimestamp;
  }

  String getVoiceInstruction() {
    return voiceInstruction;
  }

  String getVoiceInstructionTimestamp() {
    return voiceInstructionTimestamp;
  }

  private NavigationVoiceData(Parcel in) {
    voiceInstruction = in.readString();
    voiceInstructionTimestamp = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(voiceInstruction);
    dest.writeString(voiceInstructionTimestamp);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @SuppressWarnings("unused")
  public static final Creator<NavigationVoiceData> CREATOR = new Creator<NavigationVoiceData>() {
    @Override
    public NavigationVoiceData createFromParcel(Parcel in) {
      return new NavigationVoiceData(in);
    }

    @Override
    public NavigationVoiceData[] newArray(int size) {
      return new NavigationVoiceData[size];
    }
  };
}
