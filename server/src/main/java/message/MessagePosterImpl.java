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
import responses.NewPostResponse;


public class MessagePosterImpl implements MessagePoster {

    @VisibleForTesting
    static final String COLLECTION_PATH = "messages";

    private final CollectionReference messagesCollection;
    private final Storage storage;

    public MessagePosterImpl(Firestore firestore, Storage storage){
        this.messagesCollection = firestore.collection(COLLECTION_PATH);
        this.storage = storage;
    }

    public NewPostResponse postNewMessage(String userID, byte[] image, String text, double lat, double lon, String fileType){
        NewPostResponse response;
        try{

            UUID uuid = UUID.randomUUID();
            BlobId blobId = BlobId.of("magikarp-images", uuid.toString() + fileType);
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
}
