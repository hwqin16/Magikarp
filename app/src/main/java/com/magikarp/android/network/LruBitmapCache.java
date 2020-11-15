package com.magikarp.android.network;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import org.jetbrains.annotations.NotNull;

/**
 * An L1 memory cache for storing images.
 */
public class LruBitmapCache extends LruCache<String, Bitmap> {

    /**
     * Create a new LRU bitmap cache.
     *
     * @param maxSize the maximum size of the cache (in bytes)
     */
    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NotNull String key, @NotNull Bitmap value) {
        return value.getByteCount();
    }

}
