package requests;

import com.google.gson.annotations.SerializedName;

public class MessageRequest {
  @SerializedName("image_url")
  private final String imageUrl;
  private final Double latitude;
  private final Double longitude;
  private final String text;

  /**
   * Request including details for a new or updated Message.
   *
   * @param imageUrl  String url of image
   * @param text      String description of message
   * @param latitude  Double latitude of message
   * @param longitude Double longitude of message
   */
  public MessageRequest(String imageUrl, String text, Double latitude, Double longitude) {
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
    this.text = text;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public String getText() {
    return text;
  }
}
