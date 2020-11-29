package com.magikarp.android.data.model;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class DeleteMessageRequest {

  @SerializedName("id")
  private final String postId;

  @SerializedName("user_id")
  private final String userId;

  /**
   * Create a new message.
   *
   * @param userId user ID
   * @param postId post ID
   */
  public DeleteMessageRequest(@NonNull String postId, @NonNull String userId) {
    this.postId = postId;
    this.userId = userId;
  }

  /**
   * Get the post ID.
   *
   * @return the post ID
   */
  public String getPostId() {
    return postId;
  }

  /**
   * Get the post user ID.
   *
   * @return the post user ID
   */
  public String getUserId() {
    return userId;
  }

}
