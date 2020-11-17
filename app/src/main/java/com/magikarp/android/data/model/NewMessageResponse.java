package com.magikarp.android.data.model;

public class NewMessageResponse {

  private final int record_id;

  /**
   * Create a new message response.
   *
   * @param record_id new message ID
   */
  public NewMessageResponse(int record_id) {
    this.record_id = record_id;
  }

  public int getRecordId() {
    return record_id;
  }

}
