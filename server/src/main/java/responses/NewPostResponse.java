package responses;

import com.google.gson.annotations.SerializedName;

public class NewPostResponse {
  @SerializedName("response_code")
  private final int responseCode;
  @SerializedName("record_id")
  private final String recordId;
  private final String error;

  /**
   * Response for posting a new message.
   *
   * @param responseCode int http response code
   * @param recordId     String record ID that was created
   * @param error        String error message if an error occurred
   */
  public NewPostResponse(int responseCode, String recordId, String error) {
    this.responseCode = responseCode;
    this.recordId = recordId;
    this.error = error;
  }

  public String getError() {
    return error;
  }

  public String getRecordId() {
    return recordId;
  }

  public int getResponseCode() {
    return responseCode;
  }
}

