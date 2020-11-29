package com.magikarp.android.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


import android.graphics.Bitmap;
import androidx.collection.LruCache;
import org.junit.Test;

/**
 * Class for testing {@code ImageCache}.
 */
public class TestImageCache {

  @Test
  public void testImageCachePutBitmapGetBitmap() {
    LruCache<String, Bitmap> lruCache = new LruCache<>(1);
    final Bitmap bitmap = mock(Bitmap.class);

    final ImageCache imageCache = new ImageCache(lruCache);

    imageCache.putBitmap("key", bitmap);
    assertEquals(imageCache.getBitmap("key"), bitmap);
  }

}
