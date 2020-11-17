package message;

import com.google.cloud.Timestamp;
import responses.DeletePostResponse;
import responses.NewPostResponse;
import responses.UpdatePostResponse;

public interface MessagePoster {
  /**
   * Post new Message, saving the content to Firestore and the image to Cloud Storage.
   *
   * @param recordId String recordId of newly created message
   * @param userID   String User ID for who is creating the post
   * @param image    byte[] containing the image to be saved in Cloud Storage
   * @param text     String description of the Message
   * @param lat      Double latitude of the Message's geotag
   * @param lon      Double longitude of the Message's geotag
   * @param fileType String filetype for easier filetype discovery on frontend
   * @param now      Timestamp current time
   * @return NewPostResponse to be returned to client
   */
  NewPostResponse postNewMessage(
      String recordId,
      String userID,
      byte[] image,
      String text,
      double lat,
      double lon,
      String fileType,
      Timestamp now
  );

  /**
   * Update existing Message, saving the content to Firestore and the image to Cloud Storage.
   *
   * @param userID   String User ID for who is updated the post
   * @param image    byte[] containing the image to be saved in Cloud Storage
   * @param text     String description of the Message
   * @param lat      Double latitude of the Message's geotag
   * @param lon      Double longitude of the Message's geotag
   * @param fileType String filetype for easier filetype discovery on frontend
   * @param now      Timestamp current time
   * @return UpdatePostResponse to be returned to client
   */
  UpdatePostResponse updateMessage(
      String recordId,
      String userID,
      byte[] image,
      String text,
      double lat,
      double lon,
      String fileType,
      Timestamp now
  );

  /**
   * Delete Message by record ID.
   *
   * @param recordId String record ID of the Message to be deleted
   * @return DeletePostResponse to be returned to client
   */
  DeletePostResponse deleteMessage(String recordId);
}
