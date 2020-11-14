package com.magikarp.android.network;

import com.android.volley.RequestQueue;

/**
 * An interface for producing Volley request queues.
 */
public interface RequestQueueFactory {

    /**
     * Get a request queue for adding network requests.
     *
     * @return a request queue
     */
    RequestQueue getRequestQueue();

}
