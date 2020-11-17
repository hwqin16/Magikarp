package server;


import com.google.api.client.util.IOUtils;
import io.javalin.http.UploadedFile;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.*;
import responses.NewPostResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Right now this runs against our production database on real data. So the tests will start failing
 * once actual data is passing through it. We should use an emulated database or a staging database or something.
 * At minimum, we can create and then delete data in Antarctica or something for tests.
 * But, right now, when we don't have any of the write paths, we'll just use the production db.
 */
public class TestServerComponent {
  //    TODO: how can we safely start server from testing suite without having a token checked into public github?
  @BeforeAll
  public static void setup() {
//        Server.start();
  }

//    /**
//     * Runs only once before the testing starts.
//     */
//    @BeforeAll
//    public static void init() {
//        // Start Server
//        Server.main(null);
//    }


  /**
   * This test works, need to figure out how to get token into test and png
   */
//    @Test
//    @Order(1)
//    public void newPostTest() throws Exception{
//
//        InputStream file = new FileInputStream(new File("/tmp/karp.png"));
//        // Create HTTP request and get response
//        HttpResponse<String> response = Unirest.post("http://localhost:7000/message/123123/new")
//                .field("image", file, "karp.png")
//                .field("text", "test hello world")
//                .field("latitude", "3.999")
//                .field("longitude","9.222")
//                .asString();
//        // Get the response and parse the JSON
//        String postBody = (String) response.getBody();
//
//        JSONObject postBodyJson = new JSONObject(postBody);
//
//        String recordID = (String) postBodyJson.get("record_id");
//
//        HttpResponse<String> deleteResponse = Unirest.post("http://localhost:7000/message/123123/delete/" + recordID)
//                .asString();
//
//        String postDeleteBody = (String) deleteResponse.getBody();
//
//        JSONObject postDeleteBodyJson = new JSONObject(postDeleteBody);
//
//        // Make sure the move validity is false
//        assertEquals(201, postDeleteBodyJson.get("status"));
//        assertEquals(201, postBodyJson.get("status"));
//    }
  @Test
  public void TestUserMessagesEmpty() {

  }

  @AfterAll
  public static void stop() {
//        Server.stop();
  }
}
