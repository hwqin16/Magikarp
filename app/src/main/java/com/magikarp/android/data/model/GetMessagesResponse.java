package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetMessagesResponse {

  @SerializedName("record_count")
  private final int recordCount;

  @SerializedName("records")
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
