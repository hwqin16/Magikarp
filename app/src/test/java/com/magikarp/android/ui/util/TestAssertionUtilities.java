package com.magikarp.android.ui.util;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;


import com.magikarp.android.util.AssertionUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code AssertionUtilities}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAssertionUtilities {

  @Test
  public void testRequireWithNonNullArgument() {
    final Object object = mock(Object.class);

    assertSame(object, AssertionUtilities.require(object));
  }

  @Test(expected = AssertionError.class)
  public void testRequireWithNullArgument() {
    AssertionUtilities.require(null);
  }

}
