package com.mapbox.android.telemetry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SchemaTest {
  private static final String APPUSERTURNSTILE = "appUserTurnstile";
  private ArrayList<JsonObject> schemaArray;


  @Before
  public void downloadSchema() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback callback = provideACallback(latch, failureRef);

    Request request = new Request.Builder()
      .url("https://mapbox.s3.amazonaws.com/mapbox-gl-native/event-schema/mobile-event-schemas.jsonl.gz")
      .build();

    OkHttpClient client = new OkHttpClient();
    client.newCall(request).enqueue(callback);

    latch.await();
  }

  @Test
  public void checkTurnstileEvent() throws Exception {
    JsonObject schema = grabSchema(APPUSERTURNSTILE);

    System.out.println("schema: " + schema);
  }

  private void unpackSchemas(Response responseData) throws IOException, JSONException {
    ByteArrayInputStream bais = new ByteArrayInputStream(responseData.body().bytes());
    GZIPInputStream gzis = new GZIPInputStream(bais);
    InputStreamReader reader = new InputStreamReader(gzis);
    BufferedReader in = new BufferedReader(reader);

    schemaArray = new ArrayList<>();

    String readed;
    while ((readed = in.readLine()) != null) {
      JsonParser jsonParser = new JsonParser();
      JsonObject schema = (JsonObject)jsonParser.parse(readed);

      schemaArray.add(schema);
    }
  }

  private JsonObject grabSchema(String eventName) {
    for (JsonObject thisSchema: schemaArray) {
      String name = thisSchema.get("name").getAsString();
      System.out.println("name: " + name);

      if (name.equalsIgnoreCase(eventName)) {
        return thisSchema;
      }
    }

    return null;
  }

  private Callback provideACallback(final CountDownLatch latch, final AtomicBoolean failureRef) {
    Callback callback = new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        System.out.println("fail: " + exception.getMessage());
        failureRef.set(true);
        latch.countDown();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          unpackSchemas(response);
        } catch (IOException exception) {
          throw exception;
        } catch (JSONException exception) {
          exception.printStackTrace();
        } finally {
          latch.countDown();
        }
      }
    };
    return callback;
  }
}
