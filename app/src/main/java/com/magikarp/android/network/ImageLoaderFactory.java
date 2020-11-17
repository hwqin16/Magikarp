package com.magikarp.android.network;

import com.android.volley.toolbox.ImageLoader;

/**
 * An interface for producing Volley image loaders.
 */
public interface ImageLoaderFactory {

  /**
   * Get a Volley image loader.
   *
   * @return a network image loader
   */
  ImageLoader getImageLoader();

}
