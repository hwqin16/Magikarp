package com.magikarp.android.data.model;

import java.util.List;

public class GetMessagesResponse {

  private final int record_count;

  private final List<Message> records;

  /**
   * Create a get messages response.
   *
   * @param record_count number of messages
   * @param records      list of messages
   */
  public GetMessagesResponse(int record_count, List<Message> records) {
    this.record_count = record_count;
    this.records = records;
  }

  public int getRecordCount() {
    return record_count;
  }

  public List<Message> getRecords() {
    return records;
  }

}
