package responses;

import com.google.gson.annotations.SerializedName;

public class UpdatePostResponse {
  @SerializedName("response_code")
  private final int responseCode;
  private final String error;

  public UpdatePostResponse(int responseCode, String error) {
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
