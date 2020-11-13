package message;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MessageFinderImpl implements MessageFinder {
    @VisibleForTesting
    static final String COLLECTION_PATH = "messages";

    private final CollectionReference messagesCollection;

    public MessageFinderImpl(Firestore firestore) {
        this.messagesCollection = firestore.collection(COLLECTION_PATH);
    }

    @Override
    public List<Message> findByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = this.messagesCollection
                .whereEqualTo(Message.FS_USER_ID_FIELD_NAME, userId)
                .get()
                .get();

        return getMessagesFromQuerySnapshot(querySnapshot);
    }

    @Override
    public List<Message> findByLongitudeAndLatitude(
            GeoPoint lesserPoint,
            GeoPoint greaterPoint,
            int maxRecords
    ) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = this.messagesCollection
                .whereGreaterThanOrEqualTo(Message.FS_GEOTAG_FIELD_NAME, lesserPoint)
                .whereLessThanOrEqualTo(Message.FS_GEOTAG_FIELD_NAME, greaterPoint)
                .limit(maxRecords)
                .get()
                .get();

        List<Message> messages = getMessagesFromQuerySnapshot(querySnapshot);

        // Perform additional filter in-memory because Firestore doesn't actually handle GeoPoint comparisons well
        return messages
                .stream()
                .filter(message -> (message.getLatitude() > lesserPoint.getLatitude()) &&
                        (message.getLatitude() < greaterPoint.getLatitude()) &&
                        (message.getLongitude() > lesserPoint.getLongitude()) &&
                        (message.getLongitude() < greaterPoint.getLongitude())
                )
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static List<Message> getMessagesFromQuerySnapshot(QuerySnapshot querySnapshot) {
        List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

        return queryDocumentSnapshots
                .stream()
                .map(queryDocumentSnapshot -> getMessageFromDocumentData(queryDocumentSnapshot.getData()))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static Message getMessageFromDocumentData(Map<String, Object> documentData) {
        GeoPoint geoPoint = (GeoPoint) documentData.get(Message.FS_GEOTAG_FIELD_NAME);
        Timestamp timestamp = (Timestamp) documentData.get(Message.FS_TIMESTAMP_FIELD_NAME);

        return new Message(
                (String) documentData.get(Message.FS_ID_FIELD_NAME),
                (String) documentData.get(Message.FS_IMAGE_URL_FIELD_NAME),
                geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                (String) documentData.get(Message.FS_TEXT_FIELD_NAME),
                timestamp.toDate(),
                (String) documentData.get(Message.FS_USER_ID_FIELD_NAME)
        );
    }
}
