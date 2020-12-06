package com.magikarp.android.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.magikarp.android.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for the main activity.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

  /**
   * Rule for main activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> intentsRule =
      new ActivityScenarioRule<>(MainActivity.class);

  /**
   * Tests default view opens to map and that the open drawer button is visible.
   */
  @Test
  public void testDefaultViewMap() {
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed()));
  }

  /**
   * Tests the contents of the drawer when not logged in.
   */
  @Test
  public void testOpenDefaultDrawerNotLoggedIn() {
    intentsRule.getScenario().onActivity(MainActivity::setLoggedOutUi);
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.action_login)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_maps)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_settings)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_help)).check(matches(isDisplayed()));
  }

  /**
   * Tests the contents of the drawer when logged in.
   */
  @Test
  public void testOpenDefaultDrawerLoggedIn() {
    intentsRule.getScenario().onActivity(activity -> activity.setLoggedInUi("user", "email", null));
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.nav_my_posts)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_maps)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_settings)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_help)).check(matches(isDisplayed()));
    onView(withId(R.id.action_logout)).check(matches(isDisplayed()));
    onView(withId(R.id.card_view)).check(matches(isDisplayed()));
  }

  /**
   * Tests the contents of the drawer when logged in.
   */
  @Test
  public void testOpenDefaultDrawerAfterLogOut() {
    intentsRule.getScenario().onActivity(activity -> activity.setLoggedInUi("user", "email", null));
    intentsRule.getScenario().onActivity(MainActivity::setLoggedOutUi);
    onView(withId(R.id.map_container)).check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed()));
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    onView(withId(R.id.action_login)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_maps)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_settings)).check(matches(isDisplayed()));
    onView(withId(R.id.nav_help)).check(matches(isDisplayed()));
  }
}
