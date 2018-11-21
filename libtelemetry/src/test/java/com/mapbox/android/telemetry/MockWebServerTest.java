package com.mapbox.android.telemetry;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import okhttp3.mockwebserver.internal.tls.SslClient;
import okio.Buffer;
import okio.GzipSource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

class MockWebServerTest {
  private static final String FILE_ENCODING = "UTF-8";
  private MockWebServer server;
  private MockResponse mockResponse;

  @Before
  public void setUp() throws Exception {
    this.server = new MockWebServer();
    this.server.useHttps(SslClient.localhost().socketFactory, false);
    this.server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
  }

  void enqueueMockResponse() throws IOException {
    enqueueMockResponse(HttpURLConnection.HTTP_NO_CONTENT);
  }

  void enqueueMockResponse(int code) throws IOException {
    enqueueMockResponse(code, null);
  }

  void enqueueMockResponse(int code, String fileName) throws IOException {
    mockResponse = new MockResponse();
    mockResponse.setResponseCode(code);
    String fileContent = obtainContentFromFile(fileName);
    mockResponse.setBody(fileContent);
    server.enqueue(mockResponse);
  }

  void enqueueMockNoResponse(int code) throws IOException {
    mockResponse = new MockResponse();
    mockResponse.setResponseCode(code);
    mockResponse.setSocketPolicy(SocketPolicy.NO_RESPONSE);
    server.enqueue(mockResponse);
  }

  void assertRequestSentTo(String url) throws InterruptedException {
    RecordedRequest request = server.takeRequest();
    assertEquals(url, request.getRequestUrl().encodedPath());
  }

  void assertPostRequestSentTo(String url) throws InterruptedException {
    RecordedRequest request = server.takeRequest();
    assertEquals(url, request.getRequestUrl().encodedPath());
    assertEquals("POST", request.getMethod());
  }

  void assertRequestContainsHeader(String key, String expectedValue) throws InterruptedException {
    assertRequestContainsHeader(key, expectedValue, 0);
  }

  void assertRequestContainsHeader(String key, String expectedValue, int requestIndex)
    throws InterruptedException {
    RecordedRequest recordedRequest = obtainRecordedRequestAtIndex(requestIndex);
    String value = recordedRequest.getHeader(key);
    assertEquals(expectedValue, value);
  }

  void assertRequestContainsParameter(String key, String expectedValue) throws InterruptedException {
    RecordedRequest request = server.takeRequest();
    assertEquals(expectedValue, request.getRequestUrl().queryParameter(key));
  }

  HttpUrl obtainBaseEndpointUrl() {
    return server.url("/");
  }

  void assertRequestBodyEquals(String jsonFile) throws InterruptedException, IOException {
    RecordedRequest request = server.takeRequest();
    assertEquals(jsonFile, gunzip(request.getBody()).readUtf8());
  }

  void assertResponseBodyEquals(String jsonFile) throws InterruptedException, IOException {
    if (jsonFile == null) {
      assertEquals(jsonFile, mockResponse.getBody());
    } else {
      assertEquals(jsonFile, mockResponse.getBody().readUtf8());
    }
  }

  String obtainContentFromFile(String fileName) throws IOException {
    if (fileName == null) {
      return "";
    }
    fileName = getClass().getResource("/" + fileName).getFile();
    File file = new File(fileName);
    List<String> lines = FileUtils.readLines(file, FILE_ENCODING);
    StringBuilder stringBuilder = new StringBuilder();
    for (String line : lines) {
      stringBuilder.append(line);
    }
    return stringBuilder.toString();
  }

  TelemetryClient obtainATelemetryClient(String accessToken, String userAgent) {
    TelemetryClientSettings telemetryClientSettings = provideDefaultTelemetryClientSettings();
    Logger mockedLogger = mock(Logger.class);
    CertificateBlacklist mockedBlacklist = mock(CertificateBlacklist.class);
    return new TelemetryClient(accessToken, userAgent, telemetryClientSettings, mockedLogger, mockedBlacklist);
  }

  List<Event> obtainAnEvent() {
    Event theEvent = new AppUserTurnstile("anySdkIdentifier", "anySdkVersion", false);

    return obtainEvents(theEvent);
  }

  String obtainExpectedRequestBody(GsonBuilder gsonBuilder, Event... theEvents) {
    List<Event> events = Arrays.asList(theEvents);
    Gson gson = gsonBuilder.create();
    String requestBody = gson.toJson(events);

    return requestBody;
  }

  List<Event> obtainEvents(Event... theEvents) {
    return Arrays.asList(theEvents);
  }

  private RecordedRequest obtainRecordedRequestAtIndex(int requestIndex) throws InterruptedException {
    RecordedRequest request = null;
    for (int i = 0; i <= requestIndex; i++) {
      request = server.takeRequest();
    }
    return request;
  }

  private Buffer gunzip(Buffer gzipped) throws IOException {
    Buffer result = new Buffer();
    GzipSource source = new GzipSource(gzipped);
    while (source.read(result, Integer.MAX_VALUE) != -1) {
    }
    return result;
  }

  TelemetryClientSettings provideDefaultTelemetryClientSettings() {
    HttpUrl localUrl = obtainBaseEndpointUrl();
    SslClient sslClient = SslClient.localhost();

    return new TelemetryClientSettings.Builder()
      .baseUrl(localUrl)
      .sslSocketFactory(sslClient.socketFactory)
      .x509TrustManager(sslClient.trustManager)
      .hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      })
      .build();
  }

  Attachment createAttachment(String filepath) {
    String testFilePath = filepath;
    AttachmentMetadata attachmentMetadata = new AttachmentMetadata("test.jpg", "eventId", "jpg",
      "image", "sessionId-test");

    VisionEventFactory visionEventFactory = new VisionEventFactory();

    FileAttachment visionsAttachment = visionEventFactory.createFileAttachment(testFilePath,
      MediaType.parse("image/jpg"), attachmentMetadata);

    Attachment attachment = visionEventFactory.createAttachment(Event.Type.VIS_ATTACHMENT);
    attachment.addAttachment(visionsAttachment);

    return attachment;
  }

  void saveFile(Context context, String data) throws IOException {

    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt",
        Context.MODE_PRIVATE));
      outputStreamWriter.write(data);
      outputStreamWriter.close();
    } catch (IOException exception) {
      Log.e("Exception", "File write failed: " + exception.toString());
    }
  }
}
