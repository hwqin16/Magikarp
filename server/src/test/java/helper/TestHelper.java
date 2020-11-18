package helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.Timestamp;

import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import message.Message;

public class TestHelper {
  private static final Random RANDOM = new Random();

  private static final long MIN_DATE_LIMIT =
      Timestamp.MIN_VALUE.toDate().toInstant().toEpochMilli();
  private static final long MAX_DATE_LIMIT =
      Timestamp.MAX_VALUE.toDate().toInstant().toEpochMilli();
  private static final double MIN_LATITUDE_LIMIT = -90;
  private static final double MAX_LATITUDE_LIMIT = 90;
  private static final double MIN_LONGITUDE_LIMIT = -180;
  private static final double MAX_LONGITUDE_LIMIT = 180;
  private static final int LEFT_STRING_LIMIT = 97; // letter 'a'
  private static final int RIGHT_STRING_LIMIT = 122; // letter 'z'

  public static Date getRandomDate() {
    long millis = MIN_DATE_LIMIT + (long) (Math.random() * (MAX_DATE_LIMIT - MIN_DATE_LIMIT));

    return new Date(millis);
  }

  public static Double getRandomLatitude() {
    return MIN_LATITUDE_LIMIT + (Math.random() * (MAX_LATITUDE_LIMIT - MIN_LATITUDE_LIMIT));
  }

  public static Double getRandomLongitude() {
    return MIN_LONGITUDE_LIMIT + (Math.random() * (MAX_LONGITUDE_LIMIT - MIN_LONGITUDE_LIMIT));
  }

  /**
   * Get random alphabetical String of a particular length.
   * Inspired by https://www.baeldung.com/java-random-string
   *
   * @param length Length of random String
   * @return String of random alphabetical characters of specified length
   */
  public static String getRandomString(int length) {
    return RANDOM
        .ints(LEFT_STRING_LIMIT, RIGHT_STRING_LIMIT)
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  /**
   * Build a list of mock QueryDocumentSnapshots that return each of the document data
   *
   * @param documentDataList List of Strings to Objects representing document data
   * @return List of mock QueryDocumentSnapshots
   */
  public static List<QueryDocumentSnapshot> getMockQueryDocumentSnapshotsFromDocumentDataList(
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
   *
   * @return Document Data Map
   */
  public static Map<String, Object> getRandomDocumentData() {
    Map<String, Object> documentData = new HashMap<>();

    documentData
        .put(Message.FS_GEOTAG_FIELD_NAME, new GeoPoint(getRandomLatitude(), getRandomLongitude()));
    documentData.put(Message.FS_ID_FIELD_NAME, getRandomString(20));
    documentData.put(Message.FS_IMAGE_URL_FIELD_NAME, getRandomString(20));
    documentData.put(Message.FS_TIMESTAMP_FIELD_NAME, Timestamp.of(getRandomDate()));
    documentData.put(Message.FS_USER_ID_FIELD_NAME, getRandomString(20));
    return documentData;
  }
}
