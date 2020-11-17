package message;

import com.google.cloud.firestore.GeoPoint;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MessagePoster {
  NewPostResponse postNewMessage(String userID, byte[] image, String text, double lat, double lon,
                                 String fileType);

  UpdatePostResponse updateMessage(String record_id, String userID, byte[] image, String text,
                                   double lat, double lon, String fileType);


  DeletePostResponse deleteMessage(String record_id);
}
