package com.magikarp.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.mock;
import static org.mockito.Mockito.when;

import android.view.View;
import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.google.android.gms.maps.model.Marker;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.ui.app.MainActivity;
import com.magikarp.android.ui.maps.MapsFragment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PostViewTest {

  private static final String DUMMY_URI = "file://tmp/image.jpg";

  /**
   * Rule for intents activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> mainActivity =
      new ActivityScenarioRule<>(MainActivity.class);

  /**
   * Test navigating to a dummy marker and checking it's contents.
   */
  @Test
  public void testClickOnMarker() {
    final String text = "String Text";

    final Marker marker = mock(Marker.class);

    final Message message =
        new Message("someId", "someUser", DUMMY_URI, text, 0.0d, 0.0d, "timestamp");
    when(marker.getTag()).thenReturn(message);

    mainActivity.getScenario().onActivity(activity -> {
      final View mapContainer = activity.findViewById(R.id.map_container);
      final MapsFragment fragment = FragmentManager.findFragment(mapContainer);
      fragment.onMarkerClick(marker);
    });

    onView(withId(R.id.edit_text)).check(matches(isDisplayed())).check(matches(withText(text)));
  }
}
