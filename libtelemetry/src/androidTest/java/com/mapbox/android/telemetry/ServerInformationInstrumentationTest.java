package com.mapbox.android.telemetry;

import android.os.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


import static org.junit.Assert.assertEquals;

public class ServerInformationInstrumentationTest {

  @Test
  public void comServerInfoTest() {
    List<String> configurationList = new ArrayList<String>() {
      {
        add("qMkoTOaKYcVA8pWPtVKB7Kg46M5eccBMoa9WaEQVedQ=");
      }
    };
    ComServerInformation comServerInformation = new ComServerInformation();
    String anyAppInfoHostname = "some.test.url.com";
    Bundle bundle = new Bundle();
    bundle.putString("com.mapbox.ComEventsServer", anyAppInfoHostname);
    comServerInformation.setConfigurationList(configurationList);

    ServerInformation serverInformation = comServerInformation.obtainServerInformation(bundle);

    assertEquals(anyAppInfoHostname, serverInformation.getHostname());
  }
}
