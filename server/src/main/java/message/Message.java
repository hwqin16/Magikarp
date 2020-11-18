package message;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Message {
  public static final String FS_GEOTAG_FIELD_NAME = "geotag";
  public static final String FS_ID_FIELD_NAME = "id";
  public static final String FS_IMAGE_URL_FIELD_NAME = "image_url";
  public static final String FS_TEXT_FIELD_NAME = "text";
  public static final String FS_TIMESTAMP_FIELD_NAME = "timestamp";
  public static final String FS_USER_ID_FIELD_NAME = "user_id";

  private final String id;
  @SerializedName("image_url")
  private final String imageUrl;
  private final double latitude;
  private final double longitude;
  private final String text;
  private final Date timestamp;
  @SerializedName("user_id")
  private final String userId;

  /**
   * Core Message structure containing the information for users' posts.
   *
   * @param id        String Message ID
   * @param imageUrl  String URL for image
   * @param latitude  Double latitude of geotag
   * @param longitude Double longitude of geotag
   * @param text      String description of the location
   * @param timestamp Date time the message was posted
   * @param userId    String user ID
   */
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
    return (Date) this.timestamp.clone();
  }

  public String getUserId() {
    return userId;
  }
}
