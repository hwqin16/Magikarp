package server;

import org.apache.commons.io.IOUtils;
import com.google.auth.oauth2.GoogleCredentials;
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
import message.*;
import requests.FindMessagesByBoundingBoxRequest;
import responses.DeletePostResponse;
import responses.MessagesResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Server {
    private static final Gson gson = new Gson();

    private static Javalin app;
    private static MessageFinder messageFinder;
    private static MessagePoster messagePoster;

    private static void setup() {
        FileInputStream serviceAccount;
        FirebaseOptions firebaseOptions;
        StorageOptions storageOptions;

        try {

            serviceAccount = new FileInputStream(Constants.FIREBASE_SERVICE_ACCOUNT_FILE);
            firebaseOptions = FirebaseOptions
                    .builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(Constants.FIRESTORE_URL)
                    .build();

            serviceAccount = new FileInputStream(Constants.FIREBASE_SERVICE_ACCOUNT_FILE);
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
        }

        FirebaseApp.initializeApp(firebaseOptions);

        Firestore firestore = FirestoreClient.getFirestore();
        Storage storage = storageOptions.getService();

        app = Javalin.create().start(Constants.PORT);
        messageFinder = new MessageFinderImpl(firestore);
        messagePoster = new MessagePosterImpl(firestore, storage);
    }

    public static void start() {
        setup();

        app.get("/messages", ctx -> {
            FindMessagesByBoundingBoxRequest request = gson.fromJson(
                    ctx.body(),
                    FindMessagesByBoundingBoxRequest.class
            );

            Double latitudeTop = request.getLatitudeTop();
            Double latitudeBottom = request.getLatitudeBottom();
            Double longitudeLeft = request.getLongitudeLeft();
            Double longitudeRight = request.getLongitudeRight();
            Integer maxRecords = request.getMaxRecords();

            System.out.println("Getting messages for latitude_top " + latitudeTop + ", latitude_bottom " +
                    latitudeBottom + ", longitude_left " + longitudeLeft + ", latitude_bottom " + latitudeBottom +
                    ", max_records " + maxRecords);

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
                GeoPoint lesserPoint = new GeoPoint(request.getLatitudeBottom(), request.getLongitudeLeft());
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

        app.get("/messages/:user_id", ctx -> {
            String userId = ctx.pathParam("user_id");

            System.out.println("Getting messages for user_id " + userId);
            List<Message> messages = messageFinder.findByUserId(userId);

            ctx.result(gson.toJson(new MessagesResponse(messages)));
        });

        app.post("/message/:user_id/new", ctx -> {

           String userID = ctx.pathParam("user_id");
           UploadedFile picture = ctx.uploadedFile("image");

           String text = ctx.formParam("text");
           double lat = Double.parseDouble(ctx.formParam("latitude"));
           double lon = Double.parseDouble(ctx.formParam("longitude"));

           NewPostResponse response = messagePoster.postNewMessage(userID, IOUtils.toByteArray(picture.getContent()), text, lat, lon, picture.getExtension());

           ctx.result(gson.toJson(response));

        });

        app.post(" /messages/:user_id/update/:record_id", ctx -> {

            String userID = ctx.pathParam("user_id");
            String recordID = ctx.pathParam("record_id");
            UploadedFile picture = ctx.uploadedFile("image");

            String text = ctx.formParam("text");
            double lat = Double.parseDouble(ctx.formParam("latitude"));
            double lon = Double.parseDouble(ctx.formParam("longitude"));

            UpdatePostResponse response = messagePoster.updateMessage(recordID, userID, IOUtils.toByteArray(picture.getContent()), text, lat, lon, picture.getExtension());

            ctx.result(gson.toJson(response));

        });

        app.post("/message/:user_id/delete/:record_id", ctx -> {

            DeletePostResponse response = messagePoster.deleteMessage(ctx.pathParam("record_id"));

            ctx.result(gson.toJson(response));

        });

    }

    public static void stop() {
        app.stop();
    }

    public static void main(String[] args) {
        start();
    }
}