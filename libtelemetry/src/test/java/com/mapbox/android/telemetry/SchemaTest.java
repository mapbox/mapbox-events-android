package com.mapbox.android.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

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
  private static final String LOCATION = "location";
  private static final String MAP_CLICK = "map.click";
  private static final String MAP_DRAG = "map.dragend";
  private static final String MAP_LOAD = "map.load";
  private static final String NAVIGATION_ARRIVE = "navigation.arrive";
  private static final String NAVIGATION_CANCEL = "navigation.cancel";
  private static final String NAVIGATION_DEPART = "navigation.depart";
  private static final String NAVIGATION_FASTER_ROUTE = "navigation.fasterRoute";
  private static final String NAVIGATION_FEEDBACK = "navigation.feedback";
  private static final String NAVIGATION_OVERLAPPING_AUDIO = "navigation.overlappingAudio";
  private static final String NAVIGATION_REROUTE = "navigation.reroute";
  private static final String NAVIGATION_SIGNIFICANT_SPEED_DIFFERENCE = "navigation.significantSpeedDifference";
  private static final String NAVIGATION_TURNSTIL = "navigation.turnstile";
  private static final String VISIT = "visit";
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

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkLocationEvent() throws Exception {
    JsonObject schema = grabSchema(LOCATION);
    List<Field> fields = grabClassFields(LocationEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapClickEvent() throws Exception {
    JsonObject schema = grabSchema(MAP_CLICK);
    List<Field> fields = grabClassFields(MapClickEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapDragEndEvent() throws Exception {
    JsonObject schema = grabSchema(MAP_DRAG);
    List<Field> fields = grabClassFields(MapDragendEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapLoadEvent() throws Exception {
    JsonObject schema = grabSchema(MAP_LOAD);
    List<Field> fields = grabClassFields(MapLoadEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationArriveEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_ARRIVE);
    List<Field> fields = grabClassFields(NavigationArriveEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationCancelEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_CANCEL);
    List<Field> fields = grabClassFields(NavigationCancelEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationDepartEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_DEPART);
    List<Field> fields = grabClassFields(NavigationDepartEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationFasterRouteEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FASTER_ROUTE);
    List<Field> fields = grabClassFields(NavigationFasterRouteEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationFeedbackEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FEEDBACK);
    List<Field> fields = grabClassFields(NavigationFeedbackEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationRerouteEvent() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_REROUTE);
    List<Field> fields = grabClassFields(NavigationRerouteEvent.class);

    System.out.println("schema: " + schema);
    System.out.println("fields: " + fields);

    assertEquals(schema.size(), fields.size());

    schemaContainsFields(schema, fields);
  }

  private void schemaContainsFields(JsonObject schema, List<Field> fields) {
    for (int i = 0; i < fields.size(); i++) {
      String thisField = String.valueOf(fields.get(i));
      String[] fieldArray = thisField.split(" ");
      String[] typeArray = fieldArray[fieldArray.length - 2].split("\\.");
      String type = typeArray[typeArray.length - 1];

      SerializedName fieldName = fields.get(i).getAnnotation(SerializedName.class);
      JsonObject thisSchema = schema.getAsJsonObject(fieldName.value());

      System.out.println("fieldName: " + fieldName.value());
      System.out.println("thisSchema: " + thisSchema);

      assertNotNull(schema.get(fieldName.value()));

      if (thisSchema.has("type")) {
        typesMatch(thisSchema, type);
      }
    }
  }

  private void typesMatch(JsonObject schema, String type) {
    Class<? extends JsonElement> typeClass = schema.get("type").getClass();
    JsonElement jsonElement = new JsonParser().parse(type.toLowerCase());

    if (typeClass == JsonPrimitive.class) {
      JsonElement typePrimitive = schema.get("type");

      System.out.println(typePrimitive + ": " + jsonElement);
      assertTrue(typePrimitive.equals(jsonElement));
    } else {
      JsonArray arrayOfTypes = schema.getAsJsonArray("type");
      System.out.println(arrayOfTypes + ": " + jsonElement);
      assertTrue(arrayOfTypes.contains(jsonElement));
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

      if (name.equalsIgnoreCase(eventName)) {
        System.out.println("name: " + name);
        JsonObject schema = thisSchema.get("properties").getAsJsonObject();
        schema.remove("userAgent");
        return schema;
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
