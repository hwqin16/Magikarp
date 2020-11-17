package com.magikarp.android.di;

import android.content.Context;
import android.content.res.Resources;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.magikarp.android.R;
import com.magikarp.android.di.HiltQualifiers.GetMessagesUrl;
import com.magikarp.android.di.HiltQualifiers.GetUserMessagesUrl;
import com.magikarp.android.network.ImageCache;
import com.magikarp.android.network.LruBitmapCache;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;

/**
 * A Hilt module for dependency injections that are needed at the {@code Application} level or
 * below.
 */
@Module
@InstallIn(ApplicationComponent.class)
public class ApplicationModule {

  /**
   * Injector for creating a Volley request queue.
   *
   * @param applicationContext the application context
   * @return a Volley request queue
   */
  @Singleton
  @Provides
  public static RequestQueue provideRequestQueue(@ApplicationContext Context applicationContext) {
    return Volley.newRequestQueue(applicationContext);
  }

  /**
   * Injector for creating a Volley image loader.
   *
   * @param applicationContext the application context
   * @param requestQueue       a Volley request queue
   * @return a Volley image loader
   */
  @Singleton
  @Provides
  public static ImageLoader provideImageLoader(@ApplicationContext Context applicationContext,
                                               RequestQueue requestQueue) {
    final int cacheSize = applicationContext.getResources()
        .getInteger(R.integer.bitmap_cache_size);
    return new ImageLoader(requestQueue, new ImageCache(new LruBitmapCache(cacheSize)));
  }

  /**
   * Injector for creating a URL for get messages endpoint.
   *
   * @param applicationContext the application context
   * @return a URL for get messages endpoint
   */
  @GetMessagesUrl
  @Provides
  public static String provideGetMessagesUrl(
      @ApplicationContext Context applicationContext) {
    final Resources resources = applicationContext.getResources();
    return resources.getString(R.string.server_url)
        + resources.getString(R.string.server_get_messages);
  }

  /**
   * Injector for creating a URL for get user messages endpoint.
   *
   * @param applicationContext the application context
   * @return a URL for get user messages endpoint
   */
  @GetUserMessagesUrl
  @Provides
  public static String provideGetUserMessagesUrl(@ApplicationContext Context applicationContext) {
    final Resources resources = applicationContext.getResources();
    return resources.getString(R.string.server_url)
        + resources.getString(R.string.server_get_user_messages);
  }

}
