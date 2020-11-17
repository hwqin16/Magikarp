package com.magikarp.android.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterItem;
import com.magikarp.android.data.model.GetMessagesRequest;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.di.HiltQualifiers.GetMessagesUrl;
import com.magikarp.android.di.HiltQualifiers.GetUserMessagesUrl;
import com.magikarp.android.network.GsonRequest;
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

  private final String urlGetUserMessages;

  /**
   * Create a new map item repository.
   *
   * @param requestQueue queue for adding network requests
   */
  @Inject
  public MapsRepository(@NonNull RequestQueue requestQueue,
                        @NonNull @GetMessagesUrl String urlGetMessages,
                        @NonNull @GetUserMessagesUrl String urlGetUserMessages) {
    this.requestQueue = requestQueue;
    this.urlGetMessages = urlGetMessages;
    this.urlGetUserMessages = urlGetUserMessages;
  }

  public void getMessages(boolean isUserData, @NonNull LatLngBounds bounds, int maxRecords,
                          @NonNull MapClusterItemResponseListener listener,
                          @Nullable Response.ErrorListener errorListener) {
    final GetMessagesRequest body = new GetMessagesRequest(bounds.northeast.latitude,
        bounds.southwest.longitude, bounds.southwest.latitude, bounds.northeast.longitude,
        maxRecords);
    Log.i("MapsRepository", new Gson().toJson(body)); // TODO remove
    final String url = isUserData ? urlGetUserMessages : urlGetMessages; // TODO

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

    private final MapClusterItemResponseListener listener;

    /**
     * Create a new message response listener.
     *
     * @param listener listener for message responses
     */
    public GetMessagesResponseListener(@NonNull MapClusterItemResponseListener listener) {
      this.listener = listener;
    }

    @Override
    public void onResponse(GetMessagesResponse response) {
      listener.onMapClusterItemResponse(response.getMessages());
    }

  }

  /**
   * Interface for delivering message responses.
   */
  public interface MapClusterItemResponseListener {

    /**
     * Listener for message responses.
     *
     * @param messages a list of map cluster items
     */
    void onMapClusterItemResponse(List<? extends ClusterItem> messages);

  }

}
