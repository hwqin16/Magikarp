package com.magikarp.android.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.magikarp.android.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Tests for the settings fragment.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingsFragmentTest {

  /**
   * Rule for main activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> intentsRule =
      new ActivityScenarioRule<>(MainActivity.class);

  @Test
  public void testChangeDefaultContentCount() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed())).perform(click());
    onView(withId(R.id.nav_settings)).check(matches(isDisplayed())).perform(click());
    onView(withId(androidx.preference.R.id.recycler_view))
        .perform(actionOnItem(hasDescendant(withText(R.string.max_records_description)), click()));

    final int value = 10;
    onView(withText(Integer.toString(value))).perform(click());

    intentsRule.getScenario().onActivity(activity -> {
      final int maxRecords = Integer.parseInt(
          PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext())
              .getString("max_records", "0"));
      assertEquals(value, maxRecords);
    });
  }
}
