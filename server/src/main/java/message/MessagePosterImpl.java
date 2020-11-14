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

    public MessagePosterImpl(Firestore firestore, Storage storage){
        this.messagesCollection = firestore.collection(Constants.COLLECTION_PATH);
        this.storage = storage;
    }

    public DeletePostResponse deleteMessage(String recordID){
        DeletePostResponse response;
        try{

            messagesCollection.document(recordID).delete();

            response = new DeletePostResponse(201, null);
        }catch(Exception e){
            response = new DeletePostResponse(401, e.getMessage());
        }

        return response;

    }

    public NewPostResponse postNewMessage(String userID, byte[] image, String text, double lat, double lon, String fileType){
        NewPostResponse response;
        try{

            UUID uuid = UUID.randomUUID();
            BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, uuid.toString() + fileType);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, image);


            Map<String, Object> update = new HashMap<>();

            GeoPoint point = new GeoPoint(lat, lon);


            update.put("user_id", userID);
            update.put("text", text);
            update.put("geotag", point);
            update.put("id", uuid.toString());
            update.put("url", "https://storage.googleapis.com/" + "magikarp-images/" + uuid.toString()  + fileType);
            update.put("timestamp", Timestamp.now());

            ApiFuture<WriteResult> writeResult = messagesCollection.document(uuid.toString()).set(update, SetOptions.merge());
            System.out.println("Update time : " + writeResult.get().getUpdateTime());

            response = new NewPostResponse(201, uuid.toString(), null);
        }catch(Exception e){
            System.out.println("AN ERROR OCCURED");
            response = new NewPostResponse(401, null, e.getMessage());
        }

        return response;

    }

    public UpdatePostResponse updateMessage(String record_id, String userID, byte[] image, String text, double lat, double lon, String fileType){
        UpdatePostResponse response;
        try{

            BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, record_id + fileType);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, image);


            Map<String, Object> update = new HashMap<>();

            GeoPoint point = new GeoPoint(lat, lon);


            update.put("user_id", userID);
            update.put("text", text);
            update.put("geotag", point);
            update.put("id", record_id);
            update.put("url", "https://storage.googleapis.com/" + "magikarp-images/" + record_id  + fileType);
            update.put("timestamp", Timestamp.now());

            ApiFuture<WriteResult> writeResult = messagesCollection.document(record_id).set(update, SetOptions.merge());
            System.out.println("Update time : " + writeResult.get().getUpdateTime());

            response = new UpdatePostResponse(201, null);
        }catch(Exception e){
            System.out.println("AN ERROR OCCURED");
            response = new UpdatePostResponse(401,  e.getMessage());
        }

        return response;

    }
}
