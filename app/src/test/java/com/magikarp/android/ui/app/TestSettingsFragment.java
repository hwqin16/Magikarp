package com.magikarp.android.ui.app;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


import android.os.Bundle;
import com.magikarp.android.R;
import org.junit.Before;
import org.junit.Test;

/**
 * Class for testing {@code SettingsFragment}.
 */
public class TestSettingsFragment {

  private SettingsFragment fragment;

  @Before
  public void setup() {
    fragment = new SettingsFragment();
  }

  @Test
  public void testOnCreatePreferences() {
    final Bundle savedInstanceState = mock(Bundle.class);
    final String rootKey = "rootKey";
    final SettingsFragment spy = spy(fragment);
    doNothing().when(spy).setPreferencesFromResource(R.xml.preferences, rootKey);

    spy.onCreatePreferences(savedInstanceState, rootKey);

    // Confirm method completes.
  }

}
