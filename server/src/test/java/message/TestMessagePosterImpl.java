package message;

import com.google.api.core.ApiFuture;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import constants.Constants;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static helper.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.mockito.internal.stubbing.BaseStubbing;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

public class TestMessagePosterImpl {

    /**
     * Test that a message is parsed from a document data map in the expected way.
     */
    @Test
    public void testDeleteMessage() throws ExecutionException, InterruptedException{
        List<Map<String, Object>> documentDataList = Arrays.asList(
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
        Firestore mockFirestore = mock(Firestore.class);

        when(mockFirestore.collection((Constants.COLLECTION_PATH))).thenReturn(mockMessageCollection);

        DocumentReference ref = mock(DocumentReference.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);
        Storage mockStorage = mock(Storage.class);

        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.getString("url")).thenReturn("test.com/test.png");
        ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
        ApiFuture<WriteResult> write = mock(ApiFuture.class);

        when(future.get()).thenReturn(doc);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).get()).thenReturn(future);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).delete()).thenReturn(write);
        BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, "Test.png");

        when(mockStorage.delete(blobId)).thenReturn(true);
        MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);


        DeletePostResponse test = messagePoster.deleteMessage((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME));

        assertEquals(test.getResponse_code(), 201);

    }

    /**
     * Test that a message is parsed from a document data map in the expected way.
     */
    @Test
    public void testPostMessage() throws ExecutionException, InterruptedException{
        List<Map<String, Object>> documentDataList = Arrays.asList(
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
        Firestore mockFirestore = mock(Firestore.class);

        when(mockFirestore.collection((Constants.COLLECTION_PATH))).thenReturn(mockMessageCollection);

        DocumentReference ref = mock(DocumentReference.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);
        Storage mockStorage = mock(Storage.class);

        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.getString("url")).thenReturn("test.com/test.png");
        ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
        ApiFuture<WriteResult> write = mock(ApiFuture.class);

        WriteResult res = mock(WriteResult.class);
        when(write.get()).thenReturn(res);

        String userID = "Test";
        Double lon = 90.0;
        Double lat = 90.0;
        String text = "test";
        String fileType = ".png";
        byte[] image = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");

        BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME) + fileType);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();


        Map<String, Object> newPost = new HashMap<>();

        GeoPoint point = new GeoPoint(lat, lon);

        Timestamp now = Timestamp.now();

        newPost.put("user_id", userID);
        newPost.put("text", text);
        newPost.put("geotag", point);
        newPost.put("id", (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME));
        newPost.put("url", "https://storage.googleapis.com/" + "magikarp-images/" + (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)  + fileType);
        newPost.put("timestamp", now);


        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).get()).thenReturn(future);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).set(newPost, SetOptions.merge())).thenReturn(write);

        Blob blob = mock(Blob.class);

        when(mockStorage.create(blobInfo, image)).thenReturn(blob);
        MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);

        NewPostResponse test = messagePoster.postNewMessage((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME), userID, image, text, lat,  lon, fileType, now);

        assertEquals(test.getResponse_code(), 201);

    }

    /**
     * Test that a message can be updated on the server 
     */
    @Test
    public void testUpdateMessage() throws ExecutionException, InterruptedException{
        List<Map<String, Object>> documentDataList = Arrays.asList(
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
        Firestore mockFirestore = mock(Firestore.class);

        when(mockFirestore.collection((Constants.COLLECTION_PATH))).thenReturn(mockMessageCollection);

        DocumentReference ref = mock(DocumentReference.class);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);
        Storage mockStorage = mock(Storage.class);

        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.getString("url")).thenReturn("test.com/test.png");
        ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
        ApiFuture<WriteResult> write = mock(ApiFuture.class);

        WriteResult res = mock(WriteResult.class);
        when(write.get()).thenReturn(res);

        String userID = "Test";
        Double lon = 90.0;
        Double lat = 90.0;
        String text = "test";
        String fileType = ".png";
        byte[] image = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");

        BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME) + fileType);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();


        Map<String, Object> newPost = new HashMap<>();

        GeoPoint point = new GeoPoint(lat, lon);

        Timestamp now = Timestamp.now();

        newPost.put("user_id", userID);
        newPost.put("text", text);
        newPost.put("geotag", point);
        newPost.put("id", (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME));
        newPost.put("url", "https://storage.googleapis.com/" + "magikarp-images/" + (String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)  + fileType);
        newPost.put("timestamp", now);


        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).get()).thenReturn(future);
        when(mockFirestore.collection(Constants.COLLECTION_PATH).document((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).set(newPost, SetOptions.merge())).thenReturn(write);

        Blob blob = mock(Blob.class);

        when(mockStorage.create(blobInfo, image)).thenReturn(blob);
        MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);

        UpdatePostResponse test = messagePoster.updateMessage((String)documentDataList.get(0).get(Message.FS_ID_FIELD_NAME), userID, image, text, lat,  lon, fileType, now);

        assertEquals(test.getResponse_code(), 201);

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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

}
