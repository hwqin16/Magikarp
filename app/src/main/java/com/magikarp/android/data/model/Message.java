package com.magikarp.android.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable {

  private final String id;

  @SerializedName("user_id")
  private final String userId;

  @SerializedName("image_url")
  private final String imageUrl;

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
    this.userId = userId;
    this.imageUrl = imageUrl;
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
    return userId;
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

  /**
   * Get the message timestamp.
   *
   * @return the message timestamp
   */
  public String getTimestamp() {
    return timestamp;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(userId);
    dest.writeString(imageUrl);
    dest.writeString(text);
    dest.writeDouble(latitude);
    dest.writeDouble(longitude);
    dest.writeString(timestamp);
  }

  /**
   * Creator class to supporting parcelling.
   */
  public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

    public Message createFromParcel(Parcel in) {
      return new Message(in);
    }

    public Message[] newArray(int size) {
      return new Message[size];
    }

  };

  /**
   * Create a message from a parcel.
   *
   * @param in parcel to create message
   */
  protected Message(Parcel in) {
    id = in.readString();
    userId = in.readString();
    imageUrl = in.readString();
    text = in.readString();
    latitude = in.readDouble();
    longitude = in.readDouble();
    timestamp = in.readString();
  }

}
