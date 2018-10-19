package com.mapbox.android.telemetry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
  private static final String NAVIGATION_REROUTE = "navigation.reroute";
  private static ArrayList<JsonObject> schemaArray;

  @BeforeClass
  public static void downloadSchema() throws Exception {
    unpackSchemas();
  }

  @Test
  public void checkAppUserTurnstileEventSize() throws Exception {
    JsonObject schema = grabSchema(APP_USER_TURNSTILE);
    List<Field> fields = grabClassFields(AppUserTurnstile.class);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkAppUserTurnstileEventFields() throws Exception {
    JsonObject schema = grabSchema(APP_USER_TURNSTILE);
    List<Field> fields = grabClassFields(AppUserTurnstile.class);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkLocationEventSize() throws Exception {
    JsonObject schema = grabSchema(LOCATION);
    List<Field> fields = grabClassFields(LocationEvent.class);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkLocationEventFields() throws Exception {
    JsonObject schema = grabSchema(LOCATION);
    List<Field> fields = grabClassFields(LocationEvent.class);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapClickEventSize() throws Exception {
    JsonObject schema = grabSchema(MAP_CLICK);
    List<Field> fields = grabClassFields(MapClickEvent.class);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkMapClickEventFields() throws Exception {
    JsonObject schema = grabSchema(MAP_CLICK);
    List<Field> fields = grabClassFields(MapClickEvent.class);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapDragEndEventSize() throws Exception {
    JsonObject schema = grabSchema(MAP_DRAG);
    List<Field> fields = grabClassFields(MapDragendEvent.class);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkMapDragEndEventFields() throws Exception {
    JsonObject schema = grabSchema(MAP_DRAG);
    List<Field> fields = grabClassFields(MapDragendEvent.class);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkMapLoadEventSize() throws Exception {
    JsonObject schema = grabSchema(MAP_LOAD);
    List<Field> fields = grabClassFields(MapLoadEvent.class);

    //FIXME: this assertion is invalid: we should introduce a concept of mandatory/optional field to schema validation
    //assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkMapLoadEventFields() throws Exception {
    JsonObject schema = grabSchema(MAP_LOAD);
    List<Field> fields = grabClassFields(MapLoadEvent.class);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationArriveEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_ARRIVE);
    List<Field> fields = grabClassFields(NavigationArriveEvent.class);
    fields = addNavigationMetadata(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationArriveEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_ARRIVE);
    List<Field> fields = grabClassFields(NavigationArriveEvent.class);
    fields = addNavigationMetadata(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationCancelEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_CANCEL);
    List<Field> fields = grabClassFields(NavigationCancelEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationCancelData(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationCancelEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_CANCEL);
    List<Field> fields = grabClassFields(NavigationCancelEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationCancelData(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationDepartEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_DEPART);
    List<Field> fields = grabClassFields(NavigationDepartEvent.class);
    fields = addNavigationMetadata(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationDepartEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_DEPART);
    List<Field> fields = grabClassFields(NavigationDepartEvent.class);
    fields = addNavigationMetadata(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationFasterRouteEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FASTER_ROUTE);
    List<Field> fields = grabClassFields(NavigationFasterRouteEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationNewData(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationFasterRouteEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FASTER_ROUTE);
    List<Field> fields = grabClassFields(NavigationFasterRouteEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationNewData(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationFeedbackEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FEEDBACK);
    List<Field> fields = grabClassFields(NavigationFeedbackEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationFeedbackData(fields);
    fields = addNavigationLocationData(fields);
    fields = addFeedbackEventData(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationFeedbackEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_FEEDBACK);
    List<Field> fields = grabClassFields(NavigationFeedbackEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationFeedbackData(fields);
    fields = addNavigationLocationData(fields);
    fields = addFeedbackEventData(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void checkNavigationRerouteEventSize() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_REROUTE);
    List<Field> fields = grabClassFields(NavigationRerouteEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationFeedbackData(fields);
    fields = addNavigationRerouteData(fields);
    fields = addNavigationLocationData(fields);
    fields = addNavigationNewData(fields);

    assertEquals(schema.size(), fields.size());
  }

  @Test
  public void checkNavigationRerouteEventFields() throws Exception {
    JsonObject schema = grabSchema(NAVIGATION_REROUTE);
    List<Field> fields = grabClassFields(NavigationRerouteEvent.class);
    fields = addNavigationMetadata(fields);
    fields = addNavigationStepData(fields);
    fields = addNavigationFeedbackData(fields);
    fields = addNavigationRerouteData(fields);
    fields = addNavigationLocationData(fields);
    fields = addNavigationNewData(fields);

    schemaContainsFields(schema, fields);
  }

  @Test
  public void validateVisionEventFormat() {
    JsonObject schema = grabSchema(VisionObjectDetectionEvent.VIS_OBJECT_DETECTION);
    List<Field> fields = grabClassFields(VisionObjectDetectionEvent.class);
    assertEquals(schema.size(), fields.size());
    schemaContainsFields(schema, fields);
  }

  private void schemaContainsFields(JsonObject schema, List<Field> fields) {
    int distanceRemainingCount = 0;
    int durationRemainingCount = 0;

    for (int i = 0; i < fields.size(); i++) {
      String thisField = String.valueOf(fields.get(i));
      String[] fieldArray = thisField.split(" ");
      String[] typeArray = fieldArray[fieldArray.length - 2].split("\\.");
      String type = typeArray[typeArray.length - 1];

      String[] nameArray = fieldArray[fieldArray.length - 1].split("\\.");
      String field = nameArray[nameArray.length - 1];

      SerializedName serializedName = fields.get(i).getAnnotation(SerializedName.class);

      if (serializedName != null) {
        field = serializedName.value();
      }

      if (field.equalsIgnoreCase("durationRemaining")) {
        durationRemainingCount++;

        if (durationRemainingCount > 1) {
          field = "step" + field;
        }
      }

      if (field.equalsIgnoreCase("distanceRemaining")) {
        distanceRemainingCount++;

        if (distanceRemainingCount > 1) {
          field = "step" + field;
        }
      }

      JsonObject thisSchema = findSchema(schema, field);
      assertNotNull(thisSchema);

      if (thisSchema.has("type")) {
        typesMatch(thisSchema, type);
      }
    }
  }

  private JsonObject findSchema(JsonObject schema, String field) {
    JsonObject thisSchema = schema.getAsJsonObject(field);

    return thisSchema;
  }

  private void typesMatch(JsonObject schema, String type) {
    if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")
      || type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
      type = "number";
    }

    if (type.contains("[]")) {
      type = "array";
    }

    Class<? extends JsonElement> typeClass = schema.get("type").getClass();
    JsonElement jsonElement = new JsonParser().parse(type.toLowerCase());

    if (typeClass == JsonPrimitive.class) {
      JsonElement typePrimitive = schema.get("type");
      assertTrue(typePrimitive.equals(jsonElement));
    } else {
      JsonArray arrayOfTypes = schema.getAsJsonArray("type");
      assertTrue(arrayOfTypes.contains(jsonElement));
    }
  }

  private static ByteArrayInputStream getFileBytes() throws IOException {
    InputStream inputStream = SchemaTest.class.getClassLoader().getResourceAsStream("mobile-event-schemas.jsonl.gz");
    byte[] byteOut = IOUtils.toByteArray(inputStream);

    return new ByteArrayInputStream(byteOut);
  }

  private static void unpackSchemas() throws IOException {
    ByteArrayInputStream bais = getFileBytes();
    GZIPInputStream gzis = new GZIPInputStream(bais);
    InputStreamReader reader = new InputStreamReader(gzis);
    BufferedReader in = new BufferedReader(reader);

    schemaArray = new ArrayList<>();

    Gson gson = new Gson();
    String readed;
    while ((readed = in.readLine()) != null) {
      JsonObject schema = gson.fromJson(readed, JsonObject.class);
      schemaArray.add(schema);
    }
  }

  private JsonObject grabSchema(String eventName) {
    for (JsonObject thisSchema: schemaArray) {
      String name = thisSchema.get("name").getAsString();

      if (name.equalsIgnoreCase(eventName)) {
        Gson gson = new Gson();
        String schemaString = gson.toJson(thisSchema.get("properties"));
        JsonObject schema = gson.fromJson(thisSchema.get("properties"), JsonObject.class);

        if (schema.has("step")) {
          JsonObject stepJson = schema.get("step").getAsJsonObject();
          JsonObject stepProperties = stepJson.get("properties").getAsJsonObject();

          String stepPropertiesJson = gson.toJson(stepProperties);
          schemaString = generateStepSchemaString(stepPropertiesJson, schemaString);

          schema = gson.fromJson(schemaString, JsonObject.class);
          schema.remove("step");
        }

        schema.remove("userAgent");
        schema.remove("received");
        schema.remove("token");
        schema.remove("authorization");
        schema.remove("owner");
        //temporary need to work out a solution to include this data
        schema.remove("platform");

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

  private List<Field> addNavigationMetadata(List<Field> fields) {
    fields = removeField(fields, "metadata");
    fields = removeField(fields, "navigationMetadata");

    Field[] navMetadataFields = NavigationMetadata.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationStepData(List<Field> fields) {
    fields = removeField(fields, "step");
    fields = removeField(fields, "navigationstepmetada");

    Field[] navMetadataFields = NavigationStepMetadata.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationNewData(List<Field> fields) {
    fields = removeField(fields, "navigationNewData");

    Field[] navMetadataFields = NavigationNewData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationCancelData(List<Field> fields) {
    fields = removeField(fields, "cancelData");

    Field[] navMetadataFields = NavigationCancelData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationFeedbackData(List<Field> fields) {
    fields = removeField(fields, "feedbackData");

    Field[] navMetadataFields = FeedbackData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationRerouteData(List<Field> fields) {
    fields = removeField(fields, "navigationRerouteData");

    Field[] navMetadataFields = NavigationRerouteData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addNavigationLocationData(List<Field> fields) {
    fields = removeField(fields, "navigationLocationData");

    Field[] navMetadataFields = NavigationLocationData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> addFeedbackEventData(List<Field> fields) {
    fields = removeField(fields, "feedbackEventData");

    Field[] navMetadataFields = FeedbackEventData.class.getDeclaredFields();
    for (Field field : navMetadataFields) {
      if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> removeField(List<Field> fields, String fieldName) {
    for (Field field : new ArrayList<>(fields)) {
      String thisField = String.valueOf(field);
      String[] fieldArray = thisField.split("\\.");
      String simpleField = fieldArray[fieldArray.length - 1];
      if (simpleField.equalsIgnoreCase(fieldName)) {
        fields.remove(field);
      }
    }

    return fields;
  }

  private String generateStepSchemaString(String stepJson, String schemaString) {
    stepJson = stepJson.replace("\"distanceRemaining\"", "\"stepdistanceRemaining\"");
    stepJson = stepJson.replace("durationRemaining", "stepdurationRemaining");
    stepJson = stepJson.replaceFirst("\\{", ",");
    schemaString = schemaString.replaceAll("}$", "");
    schemaString = schemaString + stepJson;

    return schemaString;
  }
}
