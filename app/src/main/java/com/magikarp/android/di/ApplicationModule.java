package com.magikarp.android.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.magikarp.android.R;
import com.magikarp.android.di.HiltQualifiers.UrlGetMessages;
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
   * Injector for creating a fused location provider client.
   *
   * @param applicationContext the application context
   */
  @Provides
  public FusedLocationProviderClient provideFusedLocationProviderClient(
      @ApplicationContext Context applicationContext) {
    return LocationServices.getFusedLocationProviderClient(applicationContext);
  }

  /**
   * Injector for creating a mutable live data object.
   *
   * @return a mutable live data object
   */
  @Provides
  public static MutableLiveData<GoogleSignInAccount> provideGoogleSignInAccountLiveData() {
    return new MutableLiveData<>();
  }

  /**
   * Injector for creating a Google sign in client.
   *
   * @param applicationContext the application context
   * @return a Google sign in client
   */
  @Provides
  public static GoogleSignInClient provideGoogleSignInClient(
      @ApplicationContext Context applicationContext) {
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail().build();
    return GoogleSignIn.getClient(applicationContext, gso);
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
   * Injector for creating a location request.
   *
   * @return a location request
   */
  @Provides
  public static LocationRequest provideLocationRequest() {
    return LocationRequest.create();
  }

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
   * Injector for creating a shared preferences object.
   *
   * @param applicationContext the application context
   * @return a URL for get messages endpoint
   */
  @Provides
  public static SharedPreferences provideSharedPreferences(
      @ApplicationContext Context applicationContext) {
    return PreferenceManager.getDefaultSharedPreferences(applicationContext);
  }

  /**
   * Injector for creating a URL for get messages endpoint.
   *
   * @param applicationContext the application context
   * @return a URL for get messages endpoint
   */
  @UrlGetMessages
  @Provides
  public static String provideGetMessagesUrl(@ApplicationContext Context applicationContext) {
    final Resources resources = applicationContext.getResources();
    return resources.getString(R.string.server_url)
        + resources.getString(R.string.server_get_messages);
  }

}
