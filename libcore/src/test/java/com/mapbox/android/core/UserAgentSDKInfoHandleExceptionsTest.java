package com.mapbox.android.core;

import android.content.res.AssetManager;

import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAgentSDKInfoHandleExceptionsTest {

  @Test
  public void testSDKInformationCatchOpenAssetsIOException() throws Exception {
    AssetManager assetManager = mock(AssetManager.class);
    when(assetManager.list(anyString())).thenThrow(IOException.class);
    MapboxSdkInfoForUserAgentGenerator.getInstance(assetManager)
      .getSdkInfoForUserAgent();
  }

  @Test
  public void testSDKInformationReadInputStremIOException() throws IOException {
    AssetManager assetManager = mock(AssetManager.class);
    when(assetManager.list(anyString())).thenReturn(new String[] {"com.mapbox.android.core"});
    when(assetManager.open(anyString())).thenThrow(IOException.class);
    MapboxSdkInfoForUserAgentGenerator.getInstance(assetManager)
      .getSdkInfoForUserAgent();
  }
}
