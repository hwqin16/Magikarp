package message;

import java.util.Date;

public class Message {
  public static final String FS_GEOTAG_FIELD_NAME = "geotag";
  public static final String FS_ID_FIELD_NAME = "id";
  public static final String FS_IMAGE_URL_FIELD_NAME = "image_url";
  public static final String FS_TEXT_FIELD_NAME = "text";
  public static final String FS_TIMESTAMP_FIELD_NAME = "timestamp";
  public static final String FS_USER_ID_FIELD_NAME = "user_id";

  private final String id;
  private final String imageUrl;
  private final double latitude;
  private final double longitude;
  private final String text;
  private final Date timestamp;
  private final String userId;

  public Message(
      String id,
      String imageUrl,
      double latitude,
      double longitude,
      String text,
      Date timestamp,
      String userId
  ) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
    this.text = text;
    this.timestamp = (Date) timestamp.clone();
    this.userId = userId;
  }

  public String getId() {
    return id;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getText() {
    return text;
  }

  public Date getTimestamp() {
    Date copy = (Date) this.timestamp.clone();
    return copy;
  }

  public String getUserId() {
    return userId;
  }
}
