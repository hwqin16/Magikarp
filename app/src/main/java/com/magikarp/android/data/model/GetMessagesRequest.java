package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;

public class GetMessagesRequest {

  @SerializedName("latitude_top")
  private final double latitudeTop;

  @SerializedName("longitude_left")
  private final double longitudeLeft;

  @SerializedName("latitude_bottom")
  private final double latitudeBottom;

  @SerializedName("longitude_right")
  private final double longitudeRight;

  @SerializedName("max_records")
  private final int maxRecords;

  /**
   * Create a get messages request.
   *
   * @param latitudeTop    northernmost latitude
   * @param longitudeLeft  leftmost longitude
   * @param latitudeBottom southernmost latitude
   * @param longitudeRight rightmost longitude
   * @param maxRecords     maximum number of records
   */
  public GetMessagesRequest(double latitudeTop, double longitudeLeft, double latitudeBottom,
                            double longitudeRight, int maxRecords) {
    this.latitudeTop = latitudeTop;
    this.longitudeLeft = longitudeLeft;
    this.latitudeBottom = latitudeBottom;
    this.longitudeRight = longitudeRight;
    this.maxRecords = maxRecords;
  }

  public double getLatitudeTop() {
    return latitudeTop;
  }

  public double getlongitudeLeft() {
    return longitudeLeft;
  }

  public double getlatitudeBottom() {
    return latitudeBottom;
  }

  public double getlongitudeRight() {
    return longitudeRight;
  }

  public int getMaxRecords() {
    return maxRecords;
  }

}
