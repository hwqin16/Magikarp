package message;

import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import constants.Constants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static helper.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMessageFinderImpl {
    /**
     * Test that a message is parsed from a document data map in the expected way.
     */
    @Test
    public void testGetMessageFromDocumentData() {
        Map<String, Object> documentData = getRandomDocumentData();

        Message message = MessageFinderImpl.getMessageFromDocumentData(documentData);

        assertMessageEqualToDocumentData(message, documentData);
    }

    /**
     * Test that a QuerySnapshot is converted into the expected Messages.
     */
    @Test
    public void testGetMessagesFromQuerySnapshot() {
        List<Map<String, Object>> documentDataList = Arrays.asList(
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData()
        );
        List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
                getMockQueryDocumentSnapshotsFromDocumentDataList(documentDataList);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mockQueryDocumentSnapshots);

        List<Message> messages = MessageFinderImpl.getMessagesFromQuerySnapshot(mockQuerySnapshot);

        assertEquals(documentDataList.size(), messages.size());
        for (int i = 0; i < messages.size(); i++) {
            assertMessageEqualToDocumentData(messages.get(i), documentDataList.get(i));
        }
    }

    /**
     * Test that a findByUserId query returns Messages as expected.
     * Note: This doesn't actually test the full filtering logic.
     *   The actual findByUserId logic is tested via integration testing on a real Firestore instance.
     */
    @Test
    public void testFindByUserId() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> documentDataList = Arrays.asList(
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData()
        );
        List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
                getMockQueryDocumentSnapshotsFromDocumentDataList(documentDataList);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mockQueryDocumentSnapshots);
        SettableApiFuture<QuerySnapshot> futureMockQuerySnapshot = SettableApiFuture.create();
        futureMockQuerySnapshot.set(mockQuerySnapshot);
        Query mockQuery = mock(Query.class);
        when(mockQuery.get()).thenReturn(futureMockQuerySnapshot);
        CollectionReference mockMessageCollection = mock(CollectionReference.class);
        when(mockMessageCollection.whereEqualTo(eq(Message.FS_USER_ID_FIELD_NAME), any())).thenReturn(mockQuery);
        Firestore mockFirestore = mock(Firestore.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH)).thenReturn(mockMessageCollection);

        MessageFinderImpl messageFinder = new MessageFinderImpl(mockFirestore);

        List<Message> messages = messageFinder.findByUserId(getRandomString(20));

        assertEquals(documentDataList.size(), messages.size());
        for (int i = 0; i < messages.size(); i++) {
            assertMessageEqualToDocumentData(messages.get(i), documentDataList.get(i));
        }
    }

    @Test
    public void testFindByBoundingBoxAllFound() throws ExecutionException, InterruptedException {
        int limit = getRandomInt();
        List<Map<String, Object>> documentDataList = Arrays.asList(
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData()
        );
        List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
                getMockQueryDocumentSnapshotsFromDocumentDataList(documentDataList);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mockQueryDocumentSnapshots);
        SettableApiFuture<QuerySnapshot> futureMockQuerySnapshot = SettableApiFuture.create();
        futureMockQuerySnapshot.set(mockQuerySnapshot);
        Query mockQuery = mock(Query.class);
        when(mockQuery.whereLessThan(eq(Message.FS_GEOTAG_FIELD_NAME), any())).thenReturn(mockQuery);
        when(mockQuery.limit(limit)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(futureMockQuerySnapshot);
        CollectionReference mockMessageCollection = mock(CollectionReference.class);
        when(mockMessageCollection.whereGreaterThan(eq(Message.FS_GEOTAG_FIELD_NAME), any())).thenReturn(mockQuery);
        Firestore mockFirestore = mock(Firestore.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH)).thenReturn(mockMessageCollection);

        MessageFinderImpl messageFinder = new MessageFinderImpl(mockFirestore);

        List<Message> messages = messageFinder.findByBoundingBox(
                new GeoPoint(-90, -180),
                new GeoPoint(90, 180),
                limit
        );

        assertEquals(documentDataList.size(), messages.size());
        for (int i = 0; i < messages.size(); i++) {
            assertMessageEqualToDocumentData(messages.get(i), documentDataList.get(i));
        }
    }

    @Test
    public void testFindByBoundingBoxNoneFound() throws ExecutionException, InterruptedException {
        int limit = getRandomInt();
        List<Map<String, Object>> documentDataList = Arrays.asList(
                getRandomDocumentData(),
                getRandomDocumentData(),
                getRandomDocumentData()
        );
        List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
                getMockQueryDocumentSnapshotsFromDocumentDataList(documentDataList);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mockQueryDocumentSnapshots);
        SettableApiFuture<QuerySnapshot> futureMockQuerySnapshot = SettableApiFuture.create();
        futureMockQuerySnapshot.set(mockQuerySnapshot);
        Query mockQuery = mock(Query.class);
        when(mockQuery.whereLessThan(eq(Message.FS_GEOTAG_FIELD_NAME), any())).thenReturn(mockQuery);
        when(mockQuery.limit(limit)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(futureMockQuerySnapshot);
        CollectionReference mockMessageCollection = mock(CollectionReference.class);
        when(mockMessageCollection.whereGreaterThan(eq(Message.FS_GEOTAG_FIELD_NAME), any())).thenReturn(mockQuery);
        Firestore mockFirestore = mock(Firestore.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH)).thenReturn(mockMessageCollection);

        MessageFinderImpl messageFinder = new MessageFinderImpl(mockFirestore);

        List<Message> messages = messageFinder.findByBoundingBox(
                new GeoPoint(42, 42),
                new GeoPoint(42, 42),
                limit
        );

        assertTrue(messages.isEmpty());
    }

    /**
     * Compares a Message to a Map from Strings to Objects representing the document data returned from Firestore
     * @param message Message to be compared
     * @param documentData Map from Strings to Objects to be compared
     */
    private static void assertMessageEqualToDocumentData(Message message, Map<String, Object> documentData) {
        GeoPoint geotag = (GeoPoint) documentData.get(Message.FS_GEOTAG_FIELD_NAME);

        assertEquals(geotag.getLatitude(), message.getLatitude());
        assertEquals(geotag.getLongitude(), message.getLongitude());
        assertEquals(documentData.get(Message.FS_ID_FIELD_NAME), message.getId());
        assertEquals(documentData.get(Message.FS_IMAGE_URL_FIELD_NAME), message.getImageUrl());
        assertEquals(documentData.get(Message.FS_TEXT_FIELD_NAME), message.getText());
        assertEquals(((Timestamp) documentData.get(Message.FS_TIMESTAMP_FIELD_NAME)).toDate(), message.getTimestamp());
        assertEquals(documentData.get(Message.FS_USER_ID_FIELD_NAME), message.getUserId());
    }

    /**
     * Get a map of Strings to random Objects that represent the Document data returned from Firestore
     * @return Document Data Map
     */
    private static Map<String, Object> getRandomDocumentData() {
        Map<String, Object> documentData = new HashMap<>();

        documentData.put(Message.FS_GEOTAG_FIELD_NAME, new GeoPoint(getRandomLatitude(), getRandomLongitude()));
        documentData.put(Message.FS_ID_FIELD_NAME, getRandomString(20));
        documentData.put(Message.FS_IMAGE_URL_FIELD_NAME, getRandomString(20));
        documentData.put(Message.FS_TIMESTAMP_FIELD_NAME, Timestamp.of(getRandomDate()));
        documentData.put(Message.FS_USER_ID_FIELD_NAME, getRandomString(20));
        return documentData;
    }

    /**
     * Build a list of mock QueryDocumentSnapshots that return each of the document data
     * @param documentDataList List of Strings to Objects representing document data
     * @return List of mock QueryDocumentSnapshots
     */
    private static List<QueryDocumentSnapshot> getMockQueryDocumentSnapshotsFromDocumentDataList(
            List<Map<String, Object>> documentDataList
    ) {
        return documentDataList
                .stream()
                .map(documentData -> {
                    QueryDocumentSnapshot queryDocumentSnapshot = mock(QueryDocumentSnapshot.class);
                    when(queryDocumentSnapshot.getData()).thenReturn(documentData);
                    return queryDocumentSnapshot;
                })
                .collect(Collectors.toList());
    }
}
