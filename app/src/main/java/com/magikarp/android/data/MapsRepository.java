package com.magikarp.android.data;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.magikarp.android.data.model.GetMessagesRequest;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.di.HiltQualifiers.GetMessagesUrl;
import com.magikarp.android.network.GsonRequest;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class for generating map items.
 */
@Singleton
public class MapsRepository {

  private final RequestQueue requestQueue;

  private final String urlGetMessages;

  /**
   * Create a new map item repository.
   *
   * @param requestQueue   queue for adding network requests
   * @param urlGetMessages URL of endpoint for requesting messages
   */
  @Inject
  public MapsRepository(@NonNull RequestQueue requestQueue,
                        @NonNull @GetMessagesUrl String urlGetMessages) {
    this.requestQueue = requestQueue;
    this.urlGetMessages = urlGetMessages;
  }

  /**
   * Get messages from the maps repository.
   *
   * @param userId        ID of user
   * @param bounds        geographic bounds of query
   * @param maxRecords    maximum records to return
   * @param listener      listener for new messages
   * @param errorListener error listener
   */
  public void getMessages(@Nullable String userId, @NonNull LatLngBounds bounds, int maxRecords,
                          @NonNull MessagesListener listener,
                          @Nullable ErrorListener errorListener) {
    // Create message body.
    final GetMessagesRequest body = new GetMessagesRequest(bounds.northeast.latitude,
        bounds.southwest.longitude, bounds.southwest.latitude, bounds.northeast.longitude,
        maxRecords);
    // Build endpoint URL.
    String url;
    if (TextUtils.isEmpty(userId)) {
      url = urlGetMessages;
    } else {
      url = urlGetMessages + "/" + userId;
    }
    // Create a new GSON request.
    GsonRequest<GetMessagesResponse> request =
        new GsonRequest<>(Request.Method.POST, url, GetMessagesResponse.class,
            new Gson().toJson(body), new GetMessagesResponseListener(listener), errorListener);
    requestQueue.add(request);
  }

  /**
   * Listener for message responses.
   */
  protected static class GetMessagesResponseListener implements
      Response.Listener<GetMessagesResponse> {

    private final MessagesListener listener;

    /**
     * Create a new message response listener.
     *
     * @param listener listener for message responses
     */
    public GetMessagesResponseListener(@NonNull MessagesListener listener) {
      this.listener = listener;
    }

    @Override
    public void onResponse(GetMessagesResponse response) {
      final List<Message> messages = response.getMessages();
      listener.onMessagesChanged((messages == null) ? Collections.emptyList() : messages);
    }

  }

  /**
   * Interface for delivering messages.
   */
  public interface MessagesListener {

    /**
     * Listener for message changes.
     *
     * @param messages a list of messages
     */
    void onMessagesChanged(@NonNull List<Message> messages);

  }

}
