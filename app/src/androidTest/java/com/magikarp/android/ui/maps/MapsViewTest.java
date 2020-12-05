package com.magikarp.android.ui.maps;

import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.magikarp.android.R;
import com.magikarp.android.ui.app.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MapsViewTest {
  /**
   * Rule for intents activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> mainActivity =
      new ActivityScenarioRule<>(MainActivity.class);

  /**
   * Tests clicking on the my location button.
   */
  @Test
  public void testClickOnMyLocationVisible() {
    mainActivity.getScenario().onActivity(activity -> {
      final View mapContainer = activity.findViewById(R.id.map_container);
      final MapsFragment fragment = (MapsFragment) FragmentManager.findFragment(mapContainer);
      assertEquals(ActivityCompat.checkSelfPermission(activity.getApplicationContext(),
          Manifest.permission.ACCESS_FINE_LOCATION)
          == PackageManager.PERMISSION_GRANTED, fragment.googleMap.isMyLocationEnabled());
    });
  }
}
