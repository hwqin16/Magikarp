package com.magikarp.android.data.model;

import java.util.List;

public class GetMessagesResponse {

  private final int recordCount;

  private final List<Message> messages;

  /**
   * Create a get messages response.
   *
   * @param recordCount number of messages
   * @param messages    list of messages
   */
  public GetMessagesResponse(int recordCount, List<Message> messages) {
    this.recordCount = recordCount;
    this.messages = messages;
  }

  public int getRecordCount() {
    return recordCount;
  }

  public List<Message> getMessages() {
    return messages;
  }

}
