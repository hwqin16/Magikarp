package responses;

import message.Message;

import java.util.List;

public class NewPostResponse {
  private int status;
  private String record_id;
  private String error;

  public NewPostResponse(int response_code, String record_id, String error) {
    this.error = error;
    this.record_id = record_id;
    this.status = response_code;

  }

  public int getResponse_code() {
    return status;
  }

  public void setResponse_code(int response_code) {
    this.status = response_code;
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

