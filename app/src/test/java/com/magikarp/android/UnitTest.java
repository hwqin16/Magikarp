package com.magikarp.android;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import com.magikarp.android.network.ImageCache;
import com.magikarp.android.network.LruBitmapCache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for unit tests.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {

    @Test
    public void test_ImageCache() {
        LruCache<String, Bitmap> lruCache = new LruCache<>(1);
        final Bitmap bitmap = mock(Bitmap.class);

        final ImageCache imageCache = new ImageCache(lruCache);
        imageCache.putBitmap("key", bitmap);
        assertEquals(imageCache.getBitmap("key"), bitmap);
    }

    @Test
    public void test_LruBitmapCache() {
        final Bitmap bitmap1 = mock(Bitmap.class);
        final Bitmap bitmap2 = mock(Bitmap.class);
        when(bitmap1.getByteCount()).thenReturn(20);
        when(bitmap2.getByteCount()).thenReturn(5);

        final LruBitmapCache cache = new LruBitmapCache(100);
        cache.put("key1", bitmap1);
        cache.put("key2", bitmap2);
        assertEquals(cache.size(), 25);
    }

}
