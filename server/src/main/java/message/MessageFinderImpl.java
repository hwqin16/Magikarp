package message;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.annotations.VisibleForTesting;
import constants.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MessageFinderImpl implements MessageFinder {

  private final CollectionReference messagesCollection;

  public MessageFinderImpl(Firestore firestore) {
    this.messagesCollection = firestore.collection(Constants.COLLECTION_PATH);
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
  public List<Message> findByBoundingBox(
      GeoPoint lesserPoint,
      GeoPoint greaterPoint,
      int maxRecords,
      boolean isCrossing90Latitude,
      boolean isCrossing180Longitude
  ) throws ExecutionException, InterruptedException {
    // Firestore doesn't handle GeoPoint queries very well (only filters by longitude, doesn't
    // handle wrap-around), so perform actual filtering in-memory. Clearly this doesn't scale well,
    // but should be good enough for MVP.
    QuerySnapshot querySnapshot = this.messagesCollection.get().get();

    List<Message> messages = getMessagesFromQuerySnapshot(querySnapshot);

    return messages
        .stream()
        .filter(message -> filterMessage(
            message,
            lesserPoint,
            greaterPoint,
            isCrossing90Latitude,
            isCrossing180Longitude
        ))
        .limit(maxRecords)
        .collect(Collectors.toList());
  }

  @VisibleForTesting
  static boolean filterMessage(
      Message message,
      GeoPoint lesserPoint,
      GeoPoint greaterPoint,
      boolean isCrossing90Latitude,
      boolean isCrossing180Longitude
  ) {
    return isInsideBoundedBox(
        message.getLatitude(),
        lesserPoint.getLatitude(),
        greaterPoint.getLatitude(),
        isCrossing90Latitude
    ) && isInsideBoundedBox(
        message.getLongitude(),
        lesserPoint.getLongitude(),
        greaterPoint.getLongitude(),
        isCrossing180Longitude
    );
  }

  @VisibleForTesting
  static boolean isInsideBoundedBox(
      Double messageValue,
      Double lesserValue,
      Double greaterValue,
      boolean wrapsAroundMaxLine
  ) {
    if (wrapsAroundMaxLine) {
      return messageValue <= lesserValue || messageValue >= greaterValue;
    } else {
      return messageValue >= lesserValue && messageValue <= greaterValue;
    }
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
