package com.magikarp.android.di;

import static org.junit.Assert.assertNotNull;
import static org.robolectric.annotation.Config.OLDEST_SDK;


import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Class for testing {@code ApplicationModule}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
public class TestApplicationModule {

  private Context context;

  @Before
  public void setup() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testAll() {
    assertNotNull(ApplicationModule.provideFusedLocationProviderClient(context));
    assertNotNull(ApplicationModule.provideGoogleSignInAccountLiveData());
    assertNotNull(ApplicationModule.provideMessageListLiveData());
    assertNotNull(ApplicationModule.provideGoogleSignInClient(context));
    assertNotNull(ApplicationModule
        .provideImageLoader(context, ApplicationModule.provideRequestQueue(context)));
    assertNotNull(ApplicationModule.provideLocationRequest());
    assertNotNull(ApplicationModule.provideRequestQueue(context));
    assertNotNull(ApplicationModule.provideSharedPreferences(context));
    assertNotNull(ApplicationModule.provideGetMessagesUrl(context));
    assertNotNull(ApplicationModule.provideGetUserMessagesUrl(context));
  }

}
