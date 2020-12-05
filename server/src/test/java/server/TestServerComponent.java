package server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import helper.TestHelper;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import requests.MessageRequest;

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
  private static final TestServerComponentHelper helper = new TestServerComponentHelper();
  private static final String endpoint = "http://localhost:7000/messages/";

  @BeforeAll
  public static void setup() throws IOException {
    Server.main(new String[]{});
  }

  @Test
  @Order(1)
  public void newPostTest() throws InterruptedException {
    // Create HTTP request and get response
    HttpResponse<String> response =
        helper.writeDataToEndpoint(endpoint + helper.getUserId() + "/new");

    // Get the response and parse the JSON
    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(201, responseJson.get("response_code"));

    Thread.sleep(500); // wait a bit to ensure following tests pass
  }

  @Test
  @Order(2)
  public void getByBoundingBoxInBoxTest() {
    FindMessagesByBoundingBoxRequest request = new FindMessagesByBoundingBoxRequest(
        helper.getLatitude() - 0.5,
        helper.getLatitude() + 0.5,
        helper.getLongitude() - 0.5,
        helper.getLongitude() + 0.5,
        0
    );
    HttpResponse<String> response = Unirest.post(endpoint)
        .body(gson.toJson(request))
        .asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(0, responseJson.getInt("record_count"));
  }

  @Test
  @Order(3)
  public void getByBoundingBoxLatitudeFlippedTest() {
    FindMessagesByBoundingBoxRequest request = new FindMessagesByBoundingBoxRequest(
        helper.getLatitude() + 0.5,
        helper.getLatitude() - 0.5,
        helper.getLongitude() - 0.5,
        helper.getLongitude() + 0.5,
        0
    );
    HttpResponse<String> response = Unirest.post(endpoint)
        .body(gson.toJson(request))
        .asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(0, responseJson.getInt("record_count"));
  }

  @Test
  @Order(4)
  public void getByBoundingBoxLongitudeFlippedTest() {
    FindMessagesByBoundingBoxRequest request = new FindMessagesByBoundingBoxRequest(
        helper.getLatitude() - 0.5,
        helper.getLatitude() + 0.5,
        helper.getLongitude() + 0.5,
        helper.getLongitude() - 0.5,
        0
    );
    HttpResponse<String> response = Unirest.post(endpoint)
        .body(gson.toJson(request))
        .asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(0, responseJson.getInt("record_count"));
  }

  @Test
  @Order(5)
  public void updatePostTestFail() {
    String recordId = getFirstRecordIdFromHelper();

    // Update test helper to new values
    helper.randomUpdate();

    HttpResponse<String> response =
            helper.writeDataToEndpoint(endpoint + 2342 + "/update/" + recordId);

    JSONObject responseJson = new JSONObject(response.getBody());

    System.out.println(responseJson);
    assertEquals(404, responseJson.get("response_code"));
  }

  @Test
  @Order(6)
  public void updatePostTest() {
    String recordId = getFirstRecordIdFromHelper();

    // Update test helper to new values
    helper.randomUpdate();

    HttpResponse<String> response =
        helper.writeDataToEndpoint(endpoint + helper.getUserId() + "/update/" + recordId);

    JSONObject responseJson = new JSONObject(response.getBody());

    System.out.println(responseJson);
    assertEquals(201, responseJson.get("response_code"));
  }

  @Test
  @Order(7)
  public void getByUserIdTest() {
    HttpResponse<String> response =
        Unirest.post(endpoint + helper.getUserId()).asString();

    JSONObject responseJson = new JSONObject(response.getBody());

    assertEquals(1, responseJson.getInt("record_count"));

    JSONObject recordJson = responseJson.getJSONArray("records").getJSONObject(0);

    helper.assertResponseIsEquivalent(recordJson);
  }

  @Test
  @Order(8)
  public void deletePostTestFailed() {
    String recordId = getFirstRecordIdFromHelper();

    HttpResponse<String> deleteResponse =
            Unirest.post(endpoint + 2342 + "/delete/" + recordId).asString();

    JSONObject postDeleteBodyJson = new JSONObject(deleteResponse.getBody());

    assertEquals(404, postDeleteBodyJson.get("response_code"));
  }

  @Test
  @Order(9)
  public void deletePostTest() {
    String recordId = getFirstRecordIdFromHelper();

    HttpResponse<String> deleteResponse =
            Unirest.post(endpoint + helper.getUserId() + "/delete/" + recordId).asString();

    JSONObject postDeleteBodyJson = new JSONObject(deleteResponse.getBody());

    assertEquals(201, postDeleteBodyJson.get("response_code"));
  }

  @Test
  @Order(10)
  public void getByBoundingBoxInvalidInputsTest() {
    String invalidLatitudeBottom = "Invalid latitude_bottom";
    String invalidLatitudeTop = "Invalid latitude_top";
    String invalidLatitudeLeft = "Invalid longitude_left";
    String invalidLatitudeRight = "Invalid longitude_right";
    String invalidMaxRecords = "Invalid max_records";

    Map<FindMessagesByBoundingBoxRequest, String> testInputToOutputMap = Stream.of(
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                null,
                0.0,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeBottom
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                -91.0,
                0.0,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeBottom
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                91.0,
                0.0,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeBottom
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                null,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeTop
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                -91.0,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeTop
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                91.0,
                0.0,
                0.0,
                1
            ),
            invalidLatitudeTop
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                null,
                0.0,
                1
            ),
            invalidLatitudeLeft
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                -181.0,
                0.0,
                1
            ),
            invalidLatitudeLeft
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                181.0,
                0.0,
                1
            ),
            invalidLatitudeLeft
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                null,
                1
            ),
            invalidLatitudeRight
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                -181.0,
                1
            ),
            invalidLatitudeRight
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                181.0,
                1
            ),
            invalidLatitudeRight
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                0.0,
                null
            ),
            invalidMaxRecords
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                0.0,
                -1
            ),
            invalidMaxRecords
        ),
        new AbstractMap.SimpleEntry<>(
            new FindMessagesByBoundingBoxRequest(
                0.0,
                0.0,
                0.0,
                0.0,
                -10
            ),
            invalidMaxRecords
        )
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    for (Map.Entry<FindMessagesByBoundingBoxRequest, String> entry : testInputToOutputMap.entrySet()) {
      HttpResponse<String> response = Unirest.post(endpoint)
          .body(gson.toJson(entry.getKey()))
          .asString();

      assertEquals(entry.getValue(), response.getBody());
    }
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

    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String text;

    public TestServerComponentHelper() {
      randomUpdate();
    }

    public void randomUpdate() {
      imageUrl = TestHelper.getRandomString(10) + ".png";
      latitude = TestHelper.getRandomLatitude();
      longitude = TestHelper.getRandomLongitude();
      text = TestHelper.getRandomString(50);
    }

    public HttpResponse<String> writeDataToEndpoint(String endpoint) {
      MessageRequest messageRequest = new MessageRequest(
          helper.getImageUrl(),
          helper.getText(),
          helper.getLatitude(),
          helper.getLongitude()
      );
      return Unirest.post(endpoint).body(gson.toJson(messageRequest)).asString();
    }

    public void assertResponseIsEquivalent(JSONObject json) {
      assertEquals(helper.getUserId(), json.getString("user_id"));
      assertEquals(helper.getText(), json.getString("text"));
      assertEquals(helper.getLatitude(), json.getDouble("latitude"));
      assertEquals(helper.getLongitude(), json.getDouble("longitude"));
    }

    public String getImageUrl() {
      return imageUrl;
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
