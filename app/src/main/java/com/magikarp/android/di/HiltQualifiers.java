package com.magikarp.android.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/**
 * Qualifiers for distinguishing multiple instances of the same class used for dependency injection.
 */
public class HiltQualifiers {

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface UrlGetMessages {
  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface UrlGetUserMessages {
  }

}
