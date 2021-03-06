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
import com.magikarp.android.di.HiltQualifiers.UrlGetMessages;
import com.magikarp.android.di.HiltQualifiers.UrlGetUserMessages;
import com.magikarp.android.network.GsonRequest;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class for generating map items.
 */
@Singleton
public class MapsRepository {

  private final RequestQueue requestQueue;

  private final String urlGetMessages;

  private final String urlGetUserMessages;

  /**
   * Create a new map item repository.
   *
   * @param requestQueue   queue for adding network requests
   * @param urlGetMessages URL of endpoint for requesting messages
   */
  @Inject
  public MapsRepository(@NonNull RequestQueue requestQueue,
                        @NonNull @UrlGetMessages String urlGetMessages,
                        @NonNull @UrlGetUserMessages String urlGetUserMessages) {
    this.requestQueue = requestQueue;
    this.urlGetMessages = urlGetMessages;
    this.urlGetUserMessages = urlGetUserMessages;
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
                          @NonNull Response.Listener<GetMessagesResponse> listener,
                          @Nullable ErrorListener errorListener) {
    // Create message body.
    final GetMessagesRequest body = new GetMessagesRequest(bounds.northeast.latitude,
        bounds.southwest.longitude, bounds.southwest.latitude, bounds.northeast.longitude,
        maxRecords);
    // Build endpoint URL.
    final String url =
        TextUtils.isEmpty(userId) ? urlGetMessages : String.format(urlGetUserMessages, userId);
    // Create a new GSON request.
    final GsonRequest<GetMessagesResponse> request =
        new GsonRequest<>(Request.Method.POST, url, GetMessagesResponse.class,
            new Gson().toJson(body), listener, errorListener);
    requestQueue.add(request);
  }

}
