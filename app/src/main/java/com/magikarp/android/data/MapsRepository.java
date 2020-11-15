package com.magikarp.android.data;

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

    private final String url;

    /**
     * Create a new map item repository.
     *
     * @param requestQueue queue for adding network requests
     * @param url          URL for requests
     */
    @Inject
    public MapsRepository(@NonNull RequestQueue requestQueue, @NonNull @GetMessagesUrl String url) {
        this.requestQueue = requestQueue;
        this.url = url;
    }

    public void getMapItems(@NonNull LatLngBounds bounds, int maxRecords,
                            @NonNull MapClusterItemResponseListener listener,
                            @Nullable Response.ErrorListener errorListener) {
        final GetMessagesRequest body = new GetMessagesRequest(bounds.northeast.latitude,
                bounds.southwest.longitude, bounds.southwest.latitude, bounds.northeast.longitude,
                maxRecords);
        requestQueue.add(new GsonRequest<>(Request.Method.GET, url, GetMessagesResponse.class,
                new Gson().toJson(body), new GetMessagesResponseListener(listener), errorListener));
    }

    protected static class GetMessagesResponseListener implements
            Response.Listener<GetMessagesResponse> {

        private final MapClusterItemResponseListener listener;

        public GetMessagesResponseListener(@NonNull MapClusterItemResponseListener listener) {
            this.listener = listener;
        }

        @Override
        public void onResponse(GetMessagesResponse response) {
            listener.onMapClusterItemResponse(response.getRecords());
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
