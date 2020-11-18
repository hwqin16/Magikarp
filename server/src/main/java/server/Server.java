package server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import constants.Constants;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import message.Message;
import message.MessageFinder;
import message.MessageFinderImpl;
import message.MessagePoster;
import message.MessagePosterImpl;
import org.apache.commons.io.IOUtils;
import requests.FindMessagesByBoundingBoxRequest;
import responses.DeletePostResponse;
import responses.MessagesResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

public class Server {
  private static final Gson gson = new Gson();

  private static Javalin app;
  private static MessageFinder messageFinder;
  private static MessagePoster messagePoster;

  private static ByteArrayInputStream getServiceAccountInputStream() {
    return new ByteArrayInputStream(
        System.getenv(Constants.FIREBASE_SERVICE_ACCOUNT_ENV_VAR).getBytes(StandardCharsets.UTF_8));
  }

  private static void setup() throws IOException {
    ByteArrayInputStream serviceAccount = getServiceAccountInputStream();
    FirebaseOptions firebaseOptions;
    StorageOptions storageOptions;

    try {
      firebaseOptions = FirebaseOptions
          .builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .setDatabaseUrl(Constants.FIRESTORE_URL)
          .build();
      serviceAccount.close();
      serviceAccount = getServiceAccountInputStream();
      storageOptions = StorageOptions
          .newBuilder()
          .setProjectId(Constants.PROJECT_ID)
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to find ServiceAccount file");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to initialize Firestore");
    } finally {
      serviceAccount.close();
    }

    FirebaseApp.initializeApp(firebaseOptions);

    Firestore firestore = FirestoreClient.getFirestore();
    Storage storage = storageOptions.getService();

    app = Javalin.create().start(Constants.PORT);
    messageFinder = new MessageFinderImpl(firestore);
    messagePoster = new MessagePosterImpl(firestore, storage);
  }

  /**
   * Start the server.
   */
  public static void start() throws IOException {
    setup();

    app.post("/messages", ctx -> {
      FindMessagesByBoundingBoxRequest request = gson.fromJson(
          ctx.body(),
          FindMessagesByBoundingBoxRequest.class
      );

      Double latitudeTop = request.getLatitudeTop();
      Double latitudeBottom = request.getLatitudeBottom();
      Double longitudeLeft = request.getLongitudeLeft();
      Double longitudeRight = request.getLongitudeRight();
      Integer maxRecords = request.getMaxRecords();

      System.out.println("Getting messages for latitude_top " + latitudeTop + ", latitude_bottom "
          + latitudeBottom + ", longitude_left " + longitudeLeft + ", latitude_bottom "
          + latitudeBottom + ", max_records " + maxRecords);

      if (latitudeBottom == null || latitudeBottom < -90 || latitudeBottom > 90) {
        ctx.result("Invalid latitude_bottom");
      } else if (latitudeTop == null || latitudeTop < -90 || latitudeTop > 90) {
        ctx.result("Invalid latitude_top");
      } else if (longitudeLeft == null || longitudeLeft < -180 || longitudeLeft > 180) {
        ctx.result("Invalid longitude_left");
      } else if (longitudeRight == null || longitudeRight < -180 || longitudeRight > 180) {
        ctx.result("Invalid longitude_right");
      } else if (maxRecords == null || maxRecords < 0) {
        ctx.result("Invalid max_records");
      } else {
        GeoPoint lesserPoint =
            new GeoPoint(request.getLatitudeBottom(), request.getLongitudeLeft());
        GeoPoint greaterPoint = new GeoPoint(request.getLatitudeTop(), request.getLongitudeRight());

        boolean cross90Latitude = false;
        boolean cross180Longitude = false;
        if (lesserPoint.getLatitude() >= greaterPoint.getLatitude()) {
          cross90Latitude = true;
        }
        if (lesserPoint.getLongitude() >= greaterPoint.getLongitude()) {
          cross180Longitude = true;
        }

        List<Message> messages = messageFinder.findByBoundingBox(
            lesserPoint,
            greaterPoint,
            maxRecords,
            cross90Latitude,
            cross180Longitude
        );

        ctx.result(gson.toJson(new MessagesResponse(messages)));
      }
    });

    app.post("/messages/:user_id", ctx -> {
      String userId = ctx.pathParam("user_id");

      System.out.println("Getting messages for user_id " + userId);
      List<Message> messages = messageFinder.findByUserId(userId);

      ctx.result(gson.toJson(new MessagesResponse(messages)));
    });

    app.post("/messages/:user_id/new", ctx -> {

      String userID = ctx.pathParam("user_id");

      System.out.println("Creating new message for user " + userID);

      UploadedFile picture = ctx.uploadedFile("image");

      NewPostResponse response;

      if (picture != null) {
        String text = ctx.formParam("text");
        double lat = ctx.formParam("latitude", Double.class).get();
        double lon = ctx.formParam("longitude", Double.class).get();

        UUID uuid = UUID.randomUUID();

        response = messagePoster.postNewMessage(
            uuid.toString(),
            userID,
            IOUtils.toByteArray(picture.getContent()),
            text,
            lat,
            lon,
            picture.getExtension(),
            Timestamp.now()
        );
      } else {
        response = new NewPostResponse(401, null, "Missing new image");
      }


      ctx.result(gson.toJson(response));

    });

    app.post("/messages/:user_id/update/:record_id", ctx -> {

      // TODO validate user_id actually owns record_id
      String userID = ctx.pathParam("user_id");
      String recordID = ctx.pathParam("record_id");

      System.out.println("Updating message " + recordID + " for user " + userID);

      UploadedFile picture = ctx.uploadedFile("image");

      UpdatePostResponse response;

      if (picture != null) {
        String text = ctx.formParam("text");
        double lat = ctx.formParam("latitude", Double.class).get();
        double lon = ctx.formParam("longitude", Double.class).get();

        response = messagePoster.updateMessage(
            recordID,
            userID,
            IOUtils.toByteArray(picture.getContent()),
            text,
            lat,
            lon,
            picture.getExtension(),
            Timestamp.now()
        );
      } else {
        response = new UpdatePostResponse(401, "Missing updated image");
      }

      ctx.result(gson.toJson(response));

    });

    app.post("/messages/:user_id/delete/:record_id", ctx -> {

      // TODO validate user_id actually owns record_id
      String userId = ctx.pathParam("user_id");
      String recordId = ctx.pathParam("record_id");

      System.out.println("Deleting message " + recordId + " from user " + userId);

      DeletePostResponse response = messagePoster.deleteMessage(recordId);

      ctx.result(gson.toJson(response));

    });

  }

  public static void stop() {
    app.stop();
  }

  public static void main(String[] args) throws IOException {
    start();
  }
}