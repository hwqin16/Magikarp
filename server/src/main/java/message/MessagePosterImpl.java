package message;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import constants.Constants;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;


public class MessagePosterImpl implements MessagePoster {

  private final CollectionReference messagesCollection;

  public MessagePosterImpl(Firestore firestore) {
    this.messagesCollection = firestore.collection(Constants.COLLECTION_PATH);
  }

  @Override
  public DeletePostResponse deleteMessage(String recordId) {

    // TODO security

    messagesCollection.document(recordId).delete();

    return new DeletePostResponse(201, null);
  }

  @Override
  public NewPostResponse postNewMessage(
      String recordId,
      String userID,
      String imageUrl,
      String text,
      double lat,
      double lon,
      Timestamp now
  ) {
    NewPostResponse response;
    try {
      Map<String, Object> newPost = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);

      newPost.put(Message.FS_USER_ID_FIELD_NAME, userID);
      newPost.put(Message.FS_TEXT_FIELD_NAME, text);
      newPost.put(Message.FS_GEOTAG_FIELD_NAME, point);
      newPost.put(Message.FS_ID_FIELD_NAME, recordId);
      newPost.put(Message.FS_IMAGE_URL_FIELD_NAME, imageUrl);
      newPost.put(Message.FS_TIMESTAMP_FIELD_NAME, now);

      ApiFuture<WriteResult> writeResult =
          messagesCollection.document(recordId).set(newPost, SetOptions.merge());
      writeResult.get();

      response = new NewPostResponse(201, recordId, null);
    } catch (Exception e) {
      System.out.println("AN ERROR OCCURED");
      response = new NewPostResponse(401, null, e.getMessage());
    }

    return response;

  }

  @Override
  public UpdatePostResponse updateMessage(
      String recordId,
      String userID,
      String imageUrl,
      String text,
      double lat,
      double lon,
      Timestamp now
  ) {
    UpdatePostResponse response;
    try {
      this.deleteMessage(recordId);

      Map<String, Object> update = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);


      update.put(Message.FS_USER_ID_FIELD_NAME, userID);
      update.put(Message.FS_TEXT_FIELD_NAME, text);
      update.put(Message.FS_GEOTAG_FIELD_NAME, point);
      update.put(Message.FS_ID_FIELD_NAME, recordId);
      update.put(Message.FS_IMAGE_URL_FIELD_NAME, imageUrl);
      update.put(Message.FS_TIMESTAMP_FIELD_NAME, now);

      ApiFuture<WriteResult> writeResult =
          messagesCollection.document(recordId).set(update, SetOptions.merge());
      writeResult.get();

      response = new UpdatePostResponse(201, null);
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("AN ERROR OCCURED");
      response = new UpdatePostResponse(401, e.getMessage());
    }

    return response;

  }
}
