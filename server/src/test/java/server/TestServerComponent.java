package server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import helper.TestHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import requests.FindMessagesByBoundingBoxRequest;

/**
 * Right now this runs against our production database on real data. So the tests will start failing
 * once actual data is passing through it. We should use an emulated database or a staging database
 * or something.
 * At minimum, we can create and then delete data in Antarctica or something for tests.
 * But, right now, when we don't have any of the write paths, we'll just use the production db.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestServerComponent {
  private static final Gson gson = new Gson();
  private static final String imagePath = "src/test/resources/karp.png";
  private static final TestServerComponentHelper helper = new TestServerComponentHelper();
  private static final String endpoint = "http://localhost:7000/messages/";

  @BeforeAll
  public static void setup() throws IOException {
    Server.start();
  }

  @Test
  @Order(1)
  public void newPostTest() throws IOException {
    try (InputStream image = new FileInputStream(new File(imagePath))) {
      // Create HTTP request and get response
      HttpResponse<String> response =
          helper.writeDataToEndpoint(endpoint + helper.getUserId() + "/new", image);

      // Get the response and parse the JSON
      JSONObject responseJson = new JSONObject(response.getBody());

      assertEquals(201, responseJson.get("response_code"));
    }
  }

  @Test
  @Order(2)
  public void getByBoundingBoxTest() {
    FindMessagesByBoundingBoxRequest request = new FindMessagesByBoundingBoxRequest(
        helper.getLatitude() - 0.5,
        helper.getLatitude() + 0.5,
        helper.getLongitude() - 0.5,
        helper.getLongitude() + 0.5,
        5
    );
    HttpResponse<String> response = Unirest.post(endpoint)
        .body(gson.toJson(request))
        .asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(1, responseJson.getInt("record_count"));

    JSONObject recordJson = responseJson.getJSONArray("records").getJSONObject(0);

    helper.assertResponseIsEquivalent(recordJson);
  }

  @Test
  @Order(3)
  public void updatePostTest() throws IOException {
    String recordId = getFirstRecordIdFromHelper();

    // Update test helper to new values
    helper.randomUpdate();

    try (InputStream image = new FileInputStream(new File(imagePath))) {

      HttpResponse<String> response =
          helper.writeDataToEndpoint(endpoint + helper.getUserId() + "/update/" + recordId,
              image);

      JSONObject responseJson = new JSONObject(response.getBody());

      assertEquals(201, responseJson.get("response_code"));
    }
  }

  @Test
  @Order(4)
  public void getByUserIdTest() {
    HttpResponse<String> response =
        Unirest.post(endpoint + helper.getUserId()).asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(1, responseJson.getInt("record_count"));

    JSONObject recordJson = responseJson.getJSONArray("records").getJSONObject(0);

    helper.assertResponseIsEquivalent(recordJson);
  }

  @Test
  @Order(5)
  public void deletePostTest() {
    String recordId = getFirstRecordIdFromHelper();

    HttpResponse<String> deleteResponse =
        Unirest.post(endpoint + helper.getUserId() + "/delete/" + recordId).asString();

    JSONObject postDeleteBodyJson = new JSONObject(deleteResponse.getBody());

    assertEquals(201, postDeleteBodyJson.get("response_code"));
  }

  @AfterAll
  public static void stop() {
    Server.stop();
  }

  private String getFirstRecordIdFromHelper() {
    HttpResponse<String> getByUserIdResponse =
        Unirest.post(endpoint + helper.getUserId()).asString();

    JSONObject getByUserIdBodyJson = new JSONObject(getByUserIdResponse.getBody());

    JSONObject recordJson = getByUserIdBodyJson.getJSONArray("records").getJSONObject(0);

    return recordJson.getString("id");
  }

  private static class TestServerComponentHelper {
    private final String userId = TestHelper.getRandomString(20);

    private String imageName;
    private Double latitude;
    private Double longitude;
    private String text;

    public TestServerComponentHelper() {
      randomUpdate();
    }

    public void randomUpdate() {
      imageName = TestHelper.getRandomString(10) + ".png";
      latitude = TestHelper.getRandomLatitude();
      longitude = TestHelper.getRandomLongitude();
      text = TestHelper.getRandomString(50);
    }

    public HttpResponse<String> writeDataToEndpoint(String endpoint, InputStream image) {
      return Unirest.post(endpoint)
          .field("image", image, helper.getImageName())
          .field("text", helper.getText())
          .field("latitude", Double.toString(helper.getLatitude()))
          .field("longitude", Double.toString(helper.getLongitude()))
          .asString();
    }

    public void assertResponseIsEquivalent(JSONObject json) {
      assertEquals(helper.getUserId(), json.getString("user_id"));
      assertEquals(helper.getText(), json.getString("text"));
      assertEquals(helper.getLatitude(), json.getDouble("latitude"));
      assertEquals(helper.getLongitude(), json.getDouble("longitude"));
    }

    public String getImageName() {
      return imageName;
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public String getText() {
      return text;
    }

    public String getUserId() {
      return userId;
    }
  }
}
