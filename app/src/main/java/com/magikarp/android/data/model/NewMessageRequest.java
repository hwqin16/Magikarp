package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;

public class NewMessageRequest {

  @SerializedName("image_url")
  private final String imageUrl;

  private final String text;

  private final double latitude;

  private final double longitude;


  /**
   * Create a new message.
   *
   * @param imageUrl  image URL
   * @param text      message text
   * @param latitude  message latitude
   * @param longitude message longitude
   */
  public NewMessageRequest(String imageUrl, String text, double latitude,
                           double longitude) {
    this.imageUrl = imageUrl;
    this.text = text;
    this.latitude = latitude;
    this.longitude = longitude;
  }


  /**
   * Get the message image URL.
   *
   * @return the message image URL
   */
  public String getImageUrl() {
    return imageUrl;
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


}
