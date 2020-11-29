package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;

public class DeleteMessageResponse {

  @SerializedName("record_id")
  private final String recordId;

  /**
   * Create a new message response.
   *
   * @param recordId new message ID
   */
  public DeleteMessageResponse(String recordId) {
    this.recordId = recordId;
  }

  public String getRecordId() {
    return recordId;
  }

}
