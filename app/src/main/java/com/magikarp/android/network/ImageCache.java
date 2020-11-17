package com.magikarp.android.network;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Image cache for network requests.
 */
public class ImageCache implements ImageLoader.ImageCache {

  private final LruCache<String, Bitmap> cache;

  /**
   * Create a new image cache.
   *
   * @param cache an LRU cache to use as the backing store for the image cache.
   */
  public ImageCache(LruCache<String, Bitmap> cache) {
    this.cache = cache;
  }

  @Override
  public Bitmap getBitmap(String url) {
    return cache.get(url);
  }

  @Override
  public void putBitmap(String url, Bitmap bitmap) {
    cache.put(url, bitmap);
  }

}
