package com.magikarp.android.di;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A Hilt module for dependency injections that are needed at the {@code Activity} level or
 * below.
 */
@Module
@InstallIn(ActivityComponent.class)
public class ActivityModule {

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

}
