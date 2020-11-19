package com.magikarp.android.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import android.graphics.Bitmap;
import androidx.collection.LruCache;
import org.junit.Test;

/**
 * Class for unit tests.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NetworkTest {

//  @Test
//  public void testGsonRequestParseNetworkResponse() {
//    NetworkResponse networkResponse = mock(NetworkResponse.class);
//    doReturn("{\"record_id\":1}".getBytes()).when(networkResponse).data;
//    when(networkResponse.data).thenReturn("{\"record_id\":1}".getBytes());
//    when(networkResponse.headers).thenReturn(Collections.emptyMap());
//
//    Response.Listener<NewMessageResponse> listener = resp -> {
//    };
//    GsonRequest<NewMessageResponse> request =
//        new GsonRequest<>(Request.Method.GET, "https://www.example.com", NewMessageResponse.class,
//            null, listener, null);
//
//    Response<NewMessageResponse> response = request.parseNetworkResponse(networkResponse);
//    assertEquals(response.result.getRecordId(), 1);
//  }

  @Test
  public void testImageCachePutBitmapGetBitmap() {
    LruCache<String, Bitmap> lruCache = new LruCache<>(1);
    final Bitmap bitmap = mock(Bitmap.class);

    final ImageCache imageCache = new ImageCache(lruCache);
    imageCache.putBitmap("key", bitmap);
    assertEquals(imageCache.getBitmap("key"), bitmap);
  }

  @Test
  public void test_LruBitmapCacheSizeOf() {
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
