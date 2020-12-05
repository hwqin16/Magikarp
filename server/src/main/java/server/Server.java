package server;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.common.annotations.VisibleForTesting;
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
import java.util.Map;
import java.util.UUID;
import message.Message;
import message.MessageFinder;
import message.MessageFinderImpl;
import message.MessagePoster;
import message.MessagePosterImpl;
import requests.FindMessagesByBoundingBoxRequest;
import requests.MessageRequest;
import responses.DeletePostResponse;
import responses.MessagesResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;


public class Server {
  private static final Gson gson = new Gson();

  private static Javalin app;
  private static MessageFinder messageFinder;
  private static MessagePoster messagePoster;
  private static Firestore firestore;
  
  private Server() {
  }

  private static ByteArrayInputStream getServiceAccountInputStream() {
    return new ByteArrayInputStream(
        System.getenv(Constants.FIREBASE_SERVICE_ACCOUNT_ENV_VAR).getBytes(StandardCharsets.UTF_8));
  }

  private static void setup() throws IOException {
    ByteArrayInputStream serviceAccount = getServiceAccountInputStream();
    FirebaseOptions firebaseOptions;

    try {
      firebaseOptions = FirebaseOptions
          .builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .setDatabaseUrl(Constants.FIRESTORE_URL)
          .build();
      serviceAccount.close();
    } finally {
      serviceAccount.close();
    }

    FirebaseApp.initializeApp(firebaseOptions);

    firestore = FirestoreClient.getFirestore();

    app = Javalin.create().start(Constants.PORT);
    messageFinder = new MessageFinderImpl(firestore);
    messagePoster = new MessagePosterImpl(firestore);
  }

  /**
   * Start the server.
   */
  public static void start() {
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
          + latitudeBottom + ", longitude_left " + longitudeLeft + ", longitude_right "
          + longitudeRight + ", max_records " + maxRecords);

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
      MessageRequest messageRequest = gson.fromJson(ctx.body(), MessageRequest.class);

      System.out.println("Creating new message for user " + userID);

      NewPostResponse response;

      UUID uuid = UUID.randomUUID();

      response = messagePoster.postNewMessage(
          uuid.toString(),
          userID,
          messageRequest.getImageUrl(),
          messageRequest.getText(),
          messageRequest.getLatitude(),
          messageRequest.getLongitude(),
          Timestamp.now()
      );

      ctx.result(gson.toJson(response));
    });

    app.post("/messages/:user_id/update/:record_id", ctx -> {
      // TODO validate user_id actually owns record_id
      String userID = ctx.pathParam("user_id");
      String recordID = ctx.pathParam("record_id");
      MessageRequest messageRequest = gson.fromJson(ctx.body(), MessageRequest.class);

      Map<String, Object> values = checkUser(recordID);

      if (!values.get(Message.FS_USER_ID_FIELD_NAME).equals(userID)) {
        UpdatePostResponse response = new UpdatePostResponse(404, "You do not own this post");
        ctx.result(gson.toJson(response));
        return;
      }

      System.out.println("Updating message " + recordID + " for user " + userID);

      UpdatePostResponse response = messagePoster.updateMessage(
          recordID,
          userID,
          messageRequest.getImageUrl(),
          messageRequest.getText(),
          messageRequest.getLatitude(),
          messageRequest.getLongitude(),
          Timestamp.now()
      );

      ctx.result(gson.toJson(response));

    });

    app.post("/messages/:user_id/delete/:record_id", ctx -> {

      // TODO validate user_id actually owns record_id
      String userId = ctx.pathParam("user_id");
      String recordId = ctx.pathParam("record_id");

      Map<String, Object> values = checkUser(recordId);;

      if (!values.get(Message.FS_USER_ID_FIELD_NAME).equals(userId)) {
        DeletePostResponse response = new DeletePostResponse(404, "You do not own this post");
        ctx.result(gson.toJson(response));
        return;
      }

      System.out.println("Deleting message " + recordId + " from user " + userId);

      DeletePostResponse response = messagePoster.deleteMessage(recordId);

      ctx.result(gson.toJson(response));

    });

  }

  private static Map<String, Object> checkUser(String recordId) throws
          InterruptedException, java.util.concurrent.ExecutionException {
    CollectionReference messagesCollection = firestore.collection(Constants.COLLECTION_PATH);
    DocumentReference docRef = messagesCollection.document(recordId);
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot doc = future.get();

    Map<String, Object> values = doc.getData();
    return values;
  }

  public static void stop() {
    app.stop();
  }

  /**
   * Main method for backend.
   *
   * @param args String arguments - unused
   * @throws IOException If there are IO Exceptions during setup
   */
  public static void main(String[] args) throws IOException {
    setup();
    start();
  }
}