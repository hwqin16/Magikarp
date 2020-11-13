package com.magikarp.android.network;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

/**
 * An interface for accessing a network request queue and cache.
 */
public interface VolleyUtils {

    /**
     * Get a network image loader.
     *
     * @return a network image loader
     */
    ImageLoader getImageLoader();

    /**
     * Get a request queue for adding network requests.
     *
     * @return a request queue
     */
    RequestQueue getRequestQueue();

}
