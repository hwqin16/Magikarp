package com.magikarp.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.magikarp.android.ui.app.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * App menu bar unit tests.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AppMenuBarTest {

  /**
   * Rule for the main activity.
   */
  @Rule
  public ActivityScenarioRule<MainActivity> activityRule =
      new ActivityScenarioRule<>(MainActivity.class);

  /**
   * Tests opening the menu and clicking on maps.
   */
  @Test
  public void testClickOnMaps() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.nav_maps)).perform(click());
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
  }

  /**
   * Tests opening the menu and clicking on my posts.
   */
  @Test
  public void testClickOnMyPosts() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.nav_my_posts)).perform(click());
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
  }

  /**
   * Tests opening the menu and clicking on the settings.
   */
  @Test
  public void testClickOnSettings() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.nav_settings)).perform(click());
    onView(withId(androidx.preference.R.id.recycler_view)).check(matches(isDisplayed()));
  }

  /**
   * Tests opening the menu and clicking on help.
   */
  @Test
  public void testClickOnHelp() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.nav_help)).perform(click());
  }
}
