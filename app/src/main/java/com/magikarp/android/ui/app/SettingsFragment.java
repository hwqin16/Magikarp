package com.magikarp.android.ui.app;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.magikarp.android.R;

/**
 * A class for displaying application settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
  }

}