package message;

import com.google.cloud.firestore.GeoPoint;
import responses.NewPostResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MessagePoster {
    NewPostResponse postNewMessage(String userID, byte[] image, String text, double lat, double lon, String fileType);
}
