package responses;

import message.Message;

import java.util.List;

public class MessagesResponse {
    private final List<Message> messages;
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
