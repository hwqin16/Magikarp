package com.magikarp.android.data.model;

import com.google.gson.annotations.SerializedName;

public class DeleteMessageRequest {

  @SerializedName("id_token")
  private final String idToken;

  /**
   * Delete a message.
   *
   * @param idToken ID token for authentication with the server
   */
  public DeleteMessageRequest(String idToken) {
    this.idToken = idToken;
  }

  /**
   * Get the ID token for authentication with the server.
   *
   * @return the ID token
   */
  public String getIdToken() {
    return idToken;
  }

}
