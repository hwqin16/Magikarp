package com.magikarp.android.data.model;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Message implements ClusterItem {

  private final String id;

  private final String user_id;

  private final String image_url;

  private final String text;

  private final double latitude;

  private final double longitude;

  private final String timestamp;

  /**
   * Create a new message.
   *
   * @param id        message ID
   * @param userId    user ID
   * @param imageUrl  image URL
   * @param text      message text
   * @param latitude  message latitude
   * @param longitude message longitude
   * @param timestamp message timestamp
   */
  public Message(String id, String userId, String imageUrl, String text, double latitude,
                 double longitude, String timestamp) {
    this.id = id;
    this.user_id = userId;
    this.image_url = imageUrl;
    this.text = text;
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
  }

  /**
   * Get the message ID.
   *
   * @return the message ID
   */
  public String getId() {
    return id;
  }

  /**
   * Get the message user ID.
   *
   * @return the message user ID
   */
  public String getUserId() {
    return user_id;
  }

  /**
   * Get the message image URL.
   *
   * @return the message image URL
   */
  public String getImageUrl() {
    return image_url;
  }

  /**
   * Get the message text.
   *
   * @return the message text
   */
  public String getText() {
    return text;
  }

  /**
   * Get the message latitude.
   *
   * @return the message latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Get the message longitude.
   *
   * @return the message longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Get the message timestamp.
   *
   * @return the message timestamp
   */
  public String getTimestamp() {
    return timestamp;
  }

  @NonNull
  @Override
  public LatLng getPosition() {
    return new LatLng(getLatitude(), getLongitude());
  }

  @Override
  public String getTitle() {
    return getText();
  }

  @Override
  public String getSnippet() {
    return getText();
  }

}
