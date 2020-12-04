package com.magikarp.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.magikarp.android.ui.app.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Post fragment unit tests.
 */
@RunWith(AndroidJUnit4.class)
public class PostEditorTest {

  private static final String DUMMY_URI = "file://tmp/image.jpg";

  /**
   * Rule for intents activity
   */
  @Rule
  public ActivityScenarioRule<MainActivity> intentsRule =
      new ActivityScenarioRule<>(MainActivity.class);

  /**
   * Function to navigate to the post editor before each test.
   */
  @Before
  public void navigateToPostEditor() {
    onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click());
    intentsRule.getScenario().onActivity(activity ->
        activity.runOnUiThread(() -> {
          activity.updateSignInUi(null);
          GoogleSignInAccount account = mock(GoogleSignInAccount.class);
          when(account.getDisplayName()).thenReturn("user");
          when(account.getEmail()).thenReturn("email");
          when(account.getPhotoUrl()).thenReturn(null);
          activity.updateSignInUi(account);
        }));

    onView(withId(R.id.nav_my_posts)).perform(click());
    onView(withId(R.id.action_new_post)).perform(click());

    final Intent imageClickIntent = new Intent();
    imageClickIntent.setData(Uri.parse(DUMMY_URI));
    new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, imageClickIntent);
  }

  /**
   * Tests to make sure the toolbar buttons are visible.
   */
  @Test
  public void testToolbarButtonsVisible() {
    onView(withId(R.id.menu_upload_content)).check(matches(isDisplayed()));
    onView(withId(R.id.menu_get_location)).check(matches(isDisplayed()));
  }

  /**
   * Tests to make sure that typing works as expected.
   */
  @Test
  public void testTypeInContent() {
    final String content = "Some Content Here";
    onView(withId(R.id.menu_upload_content)).check(matches(isDisplayed()));
    onView(withId(R.id.menu_get_location)).check(matches(isDisplayed()));
    onView(withId(R.id.create_post_caption)).perform(typeText(content))
        .check(matches(isDisplayed())).check(matches(withText(content)));
  }

  /**
   * Tests to make sure that clicking on the image runs an Intent.ACTION_PICK.
   */
  @Test
  public void testTypeClickImage() {
    onView(withId(R.id.menu_upload_content)).check(matches(isDisplayed()));
    onView(withId(R.id.menu_get_location)).check(matches(isDisplayed()));
    onView(withId(R.id.create_post_local_image)).perform(click());
  }

}
