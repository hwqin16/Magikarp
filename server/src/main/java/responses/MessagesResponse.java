package responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import message.Message;

public class MessagesResponse {
  private final List<Message> messages;
  @SerializedName("record_count")
  private final int recordCount;

  public MessagesResponse(List<Message> messages) {
    this.messages = messages;
    this.recordCount = messages.size();
  }

  public List<Message> getMessages() {
    return messages;
  }

  public int getRecordCount() {
    return recordCount;
  }
}
