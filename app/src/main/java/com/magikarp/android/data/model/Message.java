package com.magikarp.android.data.model;

public class Message {

    private final int id;

    private final int user_id;

    private final String image_url;

    private final String text;

    private final double latitude;

    private final double longitude;

    private final String timestamp;

    /**
     * Create a new message.
     *
     * @param id        message ID
     * @param user_id   user ID
     * @param image_url image URL
     * @param text      message text
     * @param latitude  message latitude
     * @param longitude message longitude
     * @param timestamp message timestamp
     */
    public Message(int id, int user_id, String image_url, String text, double latitude,
                   double longitude, String timestamp) {
        this.id = id;
        this.user_id = user_id;
        this.image_url = image_url;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return user_id;
    }

    public String getImageUrl() {
        return image_url;
    }

    public String getText() {
        return text;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
