package com.magikarp.android.util;

import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for common assertions.
 */
public class AssertionUtilities {

  private AssertionUtilities() {
  }

  /**
   * Requires a value to not be null.
   *
   * @param value nullable value
   * @param <T>   type of the value
   * @return value that is not null
   */
  public static @NotNull <T> T require(@Nullable T value) {
    if (value == null) {
      throw new AssertionError("Value must not be null.");
    }
    return value;
  }
}
