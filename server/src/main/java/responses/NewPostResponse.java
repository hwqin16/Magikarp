package responses;

import message.Message;

import java.util.List;

public class NewPostResponse {
    private int response_code;
    private String record_id;
    private String error;

    public NewPostResponse(int response_code,String record_id, String error) {
        this.error = error;
        this.record_id = record_id;
        this.response_code = response_code;

    }

    public int getResponse_code() {
        return response_code;
    }

    public void setResponse_code(int response_code) {
        this.response_code = response_code;
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

