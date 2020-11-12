package server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.v1.FirestoreClient;
import com.google.gson.Gson;
import constants.Constants;
import io.javalin.Javalin;
import message.Message;
import message.MessageFinder;
import message.MessageFinderImpl;
import responses.MessagesResponse;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Server {
    private static final Gson gson = new Gson();

    private static Javalin app;
    private static MessageFinder messageFinder;

    private static void setup() {
        FirestoreOptions firestoreOptions;
        try {
             firestoreOptions = FirestoreOptions
                    .getDefaultInstance()
                    .toBuilder()
                    .setProjectId(Constants.GCP_PROJECT_ID)
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firestore");
        }
        Firestore firestore = firestoreOptions.getService();

        app = Javalin.create().start(Constants.PORT);
        messageFinder = new MessageFinderImpl(firestore);
    }

    public static void main(String[] args) {
        setup();

        app.get("/messages", ctx -> {
            Double latitudeTop = ctx.queryParam("latitude_top", Double.class).get();
            Double longitudeLeft = ctx.queryParam("longitude_left", Double.class).get();
            Double latitudeBottom = ctx.queryParam("latitude_bottom", Double.class).get();
            Double longitudeRight = ctx.queryParam("longitude_right", Double.class).get();
            Integer maxRecords = ctx.queryParam("max_records", Integer.class).get();

            GeoPoint lesserPoint = new GeoPoint(latitudeBottom, longitudeLeft);
            GeoPoint greaterPoint = new GeoPoint(latitudeTop, longitudeRight);

            List<Message> messages = messageFinder.findByLongitudeAndLatitude(
                    lesserPoint,
                    greaterPoint,
                    maxRecords
            );

            ctx.result(gson.toJson(new MessagesResponse(messages)));
        });

        app.get("/messages/:user_id", ctx -> {
            String userId = ctx.pathParam("user_id");

            List<Message> messages = messageFinder.findByUserId(userId);

            ctx.result(gson.toJson(new MessagesResponse(messages)));
        });
    }

    public static void stop() {
        app.stop();
    }
}