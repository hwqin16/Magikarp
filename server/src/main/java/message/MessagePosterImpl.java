package message;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import constants.Constants;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
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

  @Override
  public DeletePostResponse deleteMessage(String recordId) {
    DeletePostResponse response;
    try {
      ApiFuture<DocumentSnapshot> future = messagesCollection.document(recordId).get();

      DocumentSnapshot document = future.get();
      System.out.println(document);
      String url = document.getString(Message.FS_IMAGE_URL_FIELD_NAME);
      System.out.println("Boop " + url);
      String[] urlSplit = Objects.requireNonNull(url).split("/");
      String fileName = urlSplit[urlSplit.length - 1];

      messagesCollection.document(recordId).delete();
      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, fileName);
      storage.delete(blobId);

      response = new DeletePostResponse(201, null);
    } catch (InterruptedException | ExecutionException e) {
      response = new DeletePostResponse(401, e.getMessage());
    }

    return response;

  }

  @Override
  public NewPostResponse postNewMessage(
      String recordId,
      String userID,
      byte[] image,
      String text,
      double lat,
      double lon,
      String fileType,
      Timestamp now
  ) {
    NewPostResponse response;
    try {

      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, recordId + fileType);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.create(blobInfo, image);


      Map<String, Object> newPost = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);

      newPost.put(Message.FS_USER_ID_FIELD_NAME, userID);
      newPost.put(Message.FS_TEXT_FIELD_NAME, text);
      newPost.put(Message.FS_GEOTAG_FIELD_NAME, point);
      newPost.put(Message.FS_ID_FIELD_NAME, recordId);
      newPost.put(Message.FS_IMAGE_URL_FIELD_NAME,
          Constants.FULL_PROJECT_BUCKET + recordId + fileType);
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
      byte[] image,
      String text,
      double lat,
      double lon,
      String fileType,
      Timestamp now
  ) {
    UpdatePostResponse response;
    try {

      this.deleteMessage(recordId);

      BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, recordId + fileType);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.create(blobInfo, image);


      Map<String, Object> update = new HashMap<>();

      GeoPoint point = new GeoPoint(lat, lon);


      update.put(Message.FS_USER_ID_FIELD_NAME, userID);
      update.put(Message.FS_TEXT_FIELD_NAME, text);
      update.put(Message.FS_GEOTAG_FIELD_NAME, point);
      update.put(Message.FS_ID_FIELD_NAME, recordId);
      update.put(Message.FS_IMAGE_URL_FIELD_NAME,
          Constants.FULL_PROJECT_BUCKET + recordId + fileType);
      update.put(Message.FS_TIMESTAMP_FIELD_NAME, now);

      ApiFuture<WriteResult> writeResult =
          messagesCollection.document(recordId).set(update, SetOptions.merge());
      writeResult.get();

      response = new UpdatePostResponse(201, null);
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("AN ERROR OCCURED");
      System.out.println(e.getMessage());
      response = new UpdatePostResponse(401, e.getMessage());
    }

    return response;

  }
}
