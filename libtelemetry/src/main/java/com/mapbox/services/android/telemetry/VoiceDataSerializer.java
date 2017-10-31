package com.mapbox.services.android.telemetry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class VoiceDataSerializer implements JsonSerializer<NavigationVoiceData> {
  private static final String VOICE_INSTRUCTION = "voiceInstruction";
  private static final String VOICE_INSTRUCTION_TIMESTAMP = "voiceInstructionTimestamp";

  @Override
  public JsonElement serialize(NavigationVoiceData src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject voiceData = new JsonObject();
    voiceData.addProperty(VOICE_INSTRUCTION, src.getVoiceInstruction());
    voiceData.addProperty(VOICE_INSTRUCTION_TIMESTAMP, src.getVoiceInstructionTimestamp());
    return voiceData;
  }
}
