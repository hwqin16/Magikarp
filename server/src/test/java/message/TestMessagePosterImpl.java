package message;

import static helper.TestHelper.getMockQueryDocumentSnapshotsFromDocumentDataList;
import static helper.TestHelper.getRandomDocumentData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import constants.Constants;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

public class TestMessagePosterImpl {

  /**
   * Test that a message is parsed from a document data map in the expected way.
   */
  @Test
  public void testDeleteMessage() throws ExecutionException, InterruptedException {
    List<Map<String, Object>> documentDataList = Collections.singletonList(
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
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentDataList.get(0).get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.getString(Message.FS_IMAGE_URL_FIELD_NAME)).thenReturn("test.com/test.png");
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    ApiFuture<WriteResult> write = mock(ApiFuture.class);

    when(future.get()).thenReturn(doc);
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).get())
        .thenReturn(future);
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentDataList.get(0).get(Message.FS_ID_FIELD_NAME)).delete())
        .thenReturn(write);
    BlobId blobId = BlobId.of(Constants.PROJECT_BUCKET, "Test.png");

    Storage mockStorage = mock(Storage.class);
    when(mockStorage.delete(blobId)).thenReturn(true);
    MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);


    DeletePostResponse test =
        messagePoster.deleteMessage((String) documentDataList.get(0).get(Message.FS_ID_FIELD_NAME));

    assertEquals(test.getResponseCode(), 201);

  }

  /**
   * Test that a message is parsed from a document data map in the expected way.
   */
  @Test
  public void testPostMessage() throws ExecutionException, InterruptedException {
    Map<String, Object> documentData = getRandomDocumentData();

    List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
        getMockQueryDocumentSnapshotsFromDocumentDataList(Collections.singletonList(documentData));
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
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);

    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.getString(Message.FS_IMAGE_URL_FIELD_NAME)).thenReturn("test.com/test.png");
    ApiFuture<WriteResult> write = mock(ApiFuture.class);

    WriteResult res = mock(WriteResult.class);
    when(write.get()).thenReturn(res);

    String userID = "Test";
    double lon = 90.0;
    double lat = 90.0;
    String text = "test";
    String fileType = ".png";

    Map<String, Object> newPost = new HashMap<>();

    GeoPoint point = new GeoPoint(lat, lon);

    Timestamp now = Timestamp.now();

    newPost.put(Message.FS_USER_ID_FIELD_NAME, userID);
    newPost.put(Message.FS_TEXT_FIELD_NAME, text);
    newPost.put(Message.FS_GEOTAG_FIELD_NAME, point);
    newPost.put(Message.FS_ID_FIELD_NAME, documentData.get(Message.FS_ID_FIELD_NAME));
    newPost.put(Message.FS_IMAGE_URL_FIELD_NAME, Constants.FULL_PROJECT_BUCKET
        + documentData.get(Message.FS_ID_FIELD_NAME) + fileType);
    newPost.put(Message.FS_TIMESTAMP_FIELD_NAME, now);


    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME)).get())
        .thenReturn(future);
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME))
        .set(newPost, SetOptions.merge())).thenReturn(write);

    Blob blob = mock(Blob.class);
    Storage mockStorage = mock(Storage.class);
    byte[] image = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");
    BlobId blobId =
        BlobId.of(Constants.PROJECT_BUCKET, documentData.get(Message.FS_ID_FIELD_NAME) + fileType);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

    when(mockStorage.create(blobInfo, image)).thenReturn(blob);
    MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);

    NewPostResponse test = messagePoster
        .postNewMessage((String) documentData.get(Message.FS_ID_FIELD_NAME), userID,
            image, text, lat, lon, fileType, now);

    assertEquals(test.getResponseCode(), 201);

  }

  /**
   * Test that a message can be updated on the server.
   */
  @Test
  public void testUpdateMessage() throws ExecutionException, InterruptedException {
    Map<String, Object> documentData = getRandomDocumentData();

    List<QueryDocumentSnapshot> mockQueryDocumentSnapshots =
        getMockQueryDocumentSnapshotsFromDocumentDataList(Collections.singletonList(documentData));
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
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME))).thenReturn(ref);

    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.getString(Message.FS_IMAGE_URL_FIELD_NAME)).thenReturn("test.com/test.png");
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    ApiFuture<WriteResult> write = mock(ApiFuture.class);

    when(future.get()).thenReturn(doc);
    WriteResult res = mock(WriteResult.class);
    when(write.get()).thenReturn(res);

    String userID = "Test";
    double lon = 90.0;
    double lat = 90.0;
    String text = "test";
    String fileType = ".png";


    Map<String, Object> newPost = new HashMap<>();

    GeoPoint point = new GeoPoint(lat, lon);

    Timestamp now = Timestamp.now();

    newPost.put(Message.FS_USER_ID_FIELD_NAME, userID);
    newPost.put(Message.FS_TEXT_FIELD_NAME, text);
    newPost.put(Message.FS_GEOTAG_FIELD_NAME, point);
    newPost.put(Message.FS_ID_FIELD_NAME, documentData.get(Message.FS_ID_FIELD_NAME));
    newPost.put(Message.FS_IMAGE_URL_FIELD_NAME, Constants.FULL_PROJECT_BUCKET
        + documentData.get(Message.FS_ID_FIELD_NAME) + fileType);
    newPost.put(Message.FS_TIMESTAMP_FIELD_NAME, now);

    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME)).get())
        .thenReturn(future);
    when(mockFirestore.collection(Constants.COLLECTION_PATH)
        .document((String) documentData.get(Message.FS_ID_FIELD_NAME))
        .set(newPost, SetOptions.merge())).thenReturn(write);

    Blob blob = mock(Blob.class);
    Storage mockStorage = mock(Storage.class);
    byte[] image = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");
    BlobId blobId =
        BlobId.of(Constants.PROJECT_BUCKET, documentData.get(Message.FS_ID_FIELD_NAME) + fileType);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

    when(mockStorage.create(blobInfo, image)).thenReturn(blob);
    MessagePosterImpl messagePoster = new MessagePosterImpl(mockFirestore, mockStorage);

    UpdatePostResponse test = messagePoster
        .updateMessage((String) documentData.get(Message.FS_ID_FIELD_NAME), userID,
            image, text, lat, lon, fileType, now);

    assertEquals(test.getResponseCode(), 201);

  }

  /**
   * Convert hex string to byte array.
   * @param s String to be converted
   * @return byte[] converted byte array
   */
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
