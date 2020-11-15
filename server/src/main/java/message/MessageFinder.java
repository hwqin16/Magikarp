package message;

import com.google.cloud.firestore.GeoPoint;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MessageFinder {
    /**
     * Find all messages created by a particular UserId.
     * @param userId UserId whose messages are to be found
     * @return List of Messages created by the user.
     */
    List<Message> findByUserId(String userId) throws ExecutionException, InterruptedException;

    /**
     * Find all messages that have a longitude and latitude within a particular bounding box limited by
     * the passed maximum number of records.
     * @param lesserPoint Bottom-Left corner of the bounding box
     * @param greaterPoint Top-Right corner of the bounding box
     * @param maxRecords Maximum number of records to return
     * @return List of Messages within the bounding box limited by the maximum number of records.
     */
    List<Message> findByBoundingBox(
            GeoPoint lesserPoint,
            GeoPoint greaterPoint,
            int maxRecords
    ) throws ExecutionException, InterruptedException;
}
