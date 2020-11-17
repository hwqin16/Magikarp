package responses;

import com.google.gson.annotations.SerializedName;

public class DeletePostResponse {
  @SerializedName("response_code")
  private final int responseCode;
  private final String error;

  public DeletePostResponse(int responseCode, String error) {
    this.error = error;
    this.responseCode = responseCode;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getError() {
    return error;
  }
}
