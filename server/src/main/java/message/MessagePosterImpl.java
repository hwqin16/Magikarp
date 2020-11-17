package message;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.common.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import constants.Constants;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;


public class MessagePosterImpl implements MessagePoster {

  private final CollectionReference messagesCollection;
  private final Storage storage;

  public MessagePosterImpl(Firestore firestore, Storage storage) {
    this.messagesCollection = firestore.collection(Constants.COLLECTION_PATH);
    this.storage = storage;
  }

  public DeletePostResponse deleteMessage(String recordID) {
    DeletePostResponse response;
    try {
      ApiFuture<DocumentSnapshot> future = messagesCollection.document(recordID).get();

      DocumentSnapshot document = future.get();
      String url = document.getString("url");
      String[] urlSplit = url.split("/");
      String fileName = urlSplit[urlSplit.length - 1];

      messagesCollection.document(recordID).delete();
      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, fileName);
      storage.delete(blobId);

      response = new DeletePostResponse(201, null);
    } catch (Exception e) {
      response = new DeletePostResponse(401, e.getMessage());
    }

    return response;

  }

  public NewPostResponse postNewMessage(String recordID, String userID, byte[] image, String text, double lat,
                                        double lon, String fileType, Timestamp now) {
    NewPostResponse response;
    try {

      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, recordID + fileType);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.create(blobInfo, image);


      Map<String, Object> newPost = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);

      newPost.put("user_id", userID);
      newPost.put("text", text);
      newPost.put("geotag", point);
      newPost.put("id", recordID);
      newPost.put("url",
          "https://storage.googleapis.com/" + "magikarp-images/" + recordID + fileType);
      newPost.put("timestamp", now);

      ApiFuture<WriteResult> writeResult =
          messagesCollection.document(recordID).set(newPost, SetOptions.merge());
      writeResult.get();

      response = new NewPostResponse(201, recordID, null);
    } catch (Exception e) {
      System.out.println("AN ERROR OCCURED");
      response = new NewPostResponse(401, null, e.getMessage());
    }

    return response;

  }

  public UpdatePostResponse updateMessage(String record_id, String userID, byte[] image,
                                          String text, double lat, double lon, String fileType, Timestamp now) {
    UpdatePostResponse response;
    try {

      this.deleteMessage(record_id);

      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, record_id + fileType);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.create(blobInfo, image);


      Map<String, Object> update = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);


      update.put("user_id", userID);
      update.put("text", text);
      update.put("geotag", point);
      update.put("id", record_id);
      update.put("url",
          "https://storage.googleapis.com/" + "magikarp-images/" + record_id + fileType);
      update.put("timestamp", now);

      ApiFuture<WriteResult> writeResult =
          messagesCollection.document(record_id).set(update, SetOptions.merge());
      writeResult.get();

      response = new UpdatePostResponse(201, null);
    } catch (Exception e) {
      System.out.println("AN ERROR OCCURED");
      response = new UpdatePostResponse(401, e.getMessage());
    }

    return response;

  }
}
