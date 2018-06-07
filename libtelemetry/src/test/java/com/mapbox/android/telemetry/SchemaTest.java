package com.mapbox.android.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SchemaTest {
  private static final String APP_USER_TURNSTILE = "appUserTurnstile";
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
  public void checkAppUserTurnstileEvent() throws Exception {
    JsonObject schema = grabSchema(APP_USER_TURNSTILE);
    List<Field> fields = grabClassFields(AppUserTurnstile.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(fields.size(), schema.size());

    for (int i = 0; i < fields.size(); i++) {
      String thisField = String.valueOf(fields.get(i));
      String[] fieldArray = thisField.split(" ");
      String[] nameArray = fieldArray[fieldArray.length -1].split("\\.");
      String[] typeArray = fieldArray[fieldArray.length -2].split("\\.");
      String field = nameArray[nameArray.length - 1];
      String type = typeArray[typeArray.length - 1];

      JsonObject thisSchema = schema.getAsJsonObject(field);

      assertNotNull(schema.get(field));

      if (thisSchema.has("type")) {
        Class<? extends JsonElement> typeClass = thisSchema.get("type").getClass();

        System.out.println("typeClass: " + typeClass);

        JsonElement jsonElement = new JsonParser().parse(type.toLowerCase());
        System.out.println("jsonElement: " + jsonElement);

        if (typeClass == JsonPrimitive.class) {
          JsonElement typePrimitive = thisSchema.get("type");
          
          assertTrue(typePrimitive.equals(jsonElement));
          System.out.println(jsonElement + " == " + typePrimitive);
        } else {
          JsonArray arrayOfTypes = thisSchema.getAsJsonArray("type");

          assertTrue(arrayOfTypes.contains(jsonElement));
          System.out.println(jsonElement + " == " + arrayOfTypes);
        }
      }
    }
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
        return thisSchema.get("properties").getAsJsonObject();
      }
    }

    return null;
  }

  private List<Field> grabClassFields(Class aClass) {
    List<Field> fields = new ArrayList<>();
    Field[] allFields = aClass.getDeclaredFields();
    for (Field field : allFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
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
