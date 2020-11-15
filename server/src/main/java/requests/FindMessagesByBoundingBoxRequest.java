package requests;

import com.google.gson.annotations.SerializedName;

public class FindMessagesByBoundingBoxRequest {
    @SerializedName("latitude_bottom")
    private final Double latitudeBottom;
    @SerializedName("latitude_top")
    private final Double latitudeTop;
    @SerializedName("longitude_left")
    private final Double longitudeLeft;
    @SerializedName("longitude_right")
    private final Double longitudeRight;
    @SerializedName("max_records")
    private final Integer maxRecords;

    public FindMessagesByBoundingBoxRequest(
            Double latitudeBottom,
            Double latitudeTop,
            Double longitudeLeft,
            Double longitudeRight,
            Integer maxRecords
    ) {
        this.latitudeBottom = latitudeBottom;
        this.latitudeTop = latitudeTop;
        this.longitudeLeft = longitudeLeft;
        this.longitudeRight = longitudeRight;
        this.maxRecords = maxRecords;
    }

    public Double getLatitudeBottom() {
        return latitudeBottom;
    }

    public Double getLatitudeTop() {
        return latitudeTop;
    }

    public Double getLongitudeLeft() {
        return longitudeLeft;
    }

    public Double getLongitudeRight() {
        return longitudeRight;
    }

    public Integer getMaxRecords() {
        return maxRecords;
    }
}
