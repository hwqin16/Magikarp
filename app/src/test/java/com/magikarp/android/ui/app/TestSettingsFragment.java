package com.magikarp.android.ui.app;

import static org.junit.Assert.assertNotNull;
import static org.robolectric.annotation.Config.OLDEST_SDK;


import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.magikarp.android.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Class for testing {@code SettingsFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
public class TestSettingsFragment {

  @Test
  public void testOnCreatePreferences() {
    final FragmentScenario<SettingsFragment> scenario =
        FragmentScenario
            .launchInContainer(SettingsFragment.class, null);

    scenario.onFragment(fragment -> assertNotNull(
        fragment.findPreference(fragment.getString(R.string.preference_key_max_records))));
  }

}
