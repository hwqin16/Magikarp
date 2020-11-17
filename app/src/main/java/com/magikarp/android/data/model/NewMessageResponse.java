package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;

public class NewMessageResponse {

  @SerializedName("record_id")
  private final int recordId;

  /**
   * Create a new message response.
   *
   * @param recordId new message ID
   */
  public NewMessageResponse(int recordId) {
    this.recordId = recordId;
  }

  public int getRecordId() {
    return recordId;
  }

}
