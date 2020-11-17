package com.magikarp.android.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.magikarp.android.R;

/**
 * A class for creating network requests.
 */
public class VolleySingleton implements ImageLoaderFactory, RequestQueueFactory {

  private static VolleySingleton instance;

  private final ImageLoader imageLoader;

  private final RequestQueue requestQueue;

  /**
   * Create a new Volley singleton.
   *
   * @param requestQueue network request queue
   * @param imageLoader  network image loader
   */
  private VolleySingleton(RequestQueue requestQueue, ImageLoader imageLoader) {
    this.requestQueue = requestQueue;
    this.imageLoader = imageLoader;
  }

  /**
   * Get a singleton instance of Volley singleton.
   *
   * @param context the context used to access resources, internally the application context is
   *                used to avoid memory leaks
   * @return a singleton instance of Volley singleton
   */
  public static synchronized VolleySingleton getInstance(Context context) {
    if (instance == null) {
      final Context applicationContext = context.getApplicationContext();
      final RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
      final int cacheSize = applicationContext.getResources()
          .getInteger(R.integer.bitmap_cache_size);
      final ImageLoader imageLoader = new ImageLoader(requestQueue,
          new ImageCache(new LruBitmapCache(cacheSize)));
      instance = new VolleySingleton(requestQueue, imageLoader);
    }
    return instance;
  }

  @Override
  public ImageLoader getImageLoader() {
    return imageLoader;
  }

  @Override
  public RequestQueue getRequestQueue() {
    return requestQueue;
  }

}
