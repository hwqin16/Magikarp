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

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A Hilt module for dependency injections that are needed at the {@code Application} level or
 * below.
 */
@Module
@InstallIn(ApplicationComponent.class)
public class ApplicationModule {

    @Singleton
    @Provides
    public static RequestQueue provideRequestQueue(@ApplicationContext Context applicationContext) {
        return Volley.newRequestQueue(applicationContext);
    }

    @Singleton
    @Provides
    public static ImageLoader provideImageLoader(@ApplicationContext Context applicationContext,
                                                 RequestQueue requestQueue) {
        final int cacheSize = applicationContext.getResources()
                .getInteger(R.integer.bitmap_cache_size);
        return new ImageLoader(requestQueue, new ImageCache(new LruBitmapCache(cacheSize)));
    }

    @GetMessagesUrl
    @Provides
    public static String provideGetMessagesUrl(
            @ApplicationContext Context applicationContext) {
        final Resources resources = applicationContext.getResources();
        return resources.getString(R.string.server_url)
                + resources.getString(R.string.server_get_messages);
    }

    @GetUserMessagesUrl
    @Provides
    public static String provideGetUserMessagesUrl(
            @ApplicationContext Context applicationContext) {
        final Resources resources = applicationContext.getResources();
        return resources.getString(R.string.server_url)
                + resources.getString(R.string.server_get_user_messages);
    }

}