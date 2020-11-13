package helper;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.Random;

public class TestHelper {
    private static final Random RANDOM = new Random();

    private static final long MIN_DATE_LIMIT = Timestamp.MIN_VALUE.toDate().toInstant().toEpochMilli();
    private static final long MAX_DATE_LIMIT = Timestamp.MAX_VALUE.toDate().toInstant().toEpochMilli();
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

    public static int getRandomInt() {
        return RANDOM.nextInt();
    }

    /**
     * Get random alphabetical String of a particular length.
     * Inspired by https://www.baeldung.com/java-random-string
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
}
