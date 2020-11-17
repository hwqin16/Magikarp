package com.magikarp.android.data.model;

public class GetMessagesRequest {

  private final double latitude_top;

  private final double longitude_left;

  private final double latitude_bottom;

  private final double longitude_right;

  private final int max_records;

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
    latitude_top = latitudeTop;
    longitude_left = longitudeLeft;
    latitude_bottom = latitudeBottom;
    longitude_right = longitudeRight;
    max_records = maxRecords;
  }

  public double getLatitudeTop() {
    return latitude_top;
  }

  public double getlongitudeLeft() {
    return longitude_left;
  }

  public double getlatitudeBottom() {
    return latitude_bottom;
  }

  public double getlongitudeRight() {
    return longitude_right;
  }

  public int getMaxRecords() {
    return max_records;
  }

}
