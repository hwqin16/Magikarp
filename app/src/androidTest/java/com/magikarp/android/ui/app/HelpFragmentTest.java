package com.magikarp.android.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.magikarp.android.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Tests for the help fragment.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class HelpFragmentTest {

  /**
   * Rule for main activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> intentsRule =
      new ActivityScenarioRule<>(MainActivity.class);

  @Test
  public void testClickCall() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description))
        .check(matches(isDisplayed())).perform(click());
    onView(withId(R.id.nav_help)).check(matches(isDisplayed())).perform(click());

    Intent intent = new Intent();
    Instrumentation.ActivityResult result =
        new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
    init();

    onView(withId(R.id.call_container)).check(matches(isDisplayed())).perform(click());

    intended(allOf(hasAction(Intent.ACTION_DIAL), hasData("tel:1–800–555–5555")));
  }
}
