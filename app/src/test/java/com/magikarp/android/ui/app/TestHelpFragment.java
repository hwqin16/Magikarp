package com.magikarp.android.ui.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.Config.OLDEST_SDK;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.magikarp.android.R;
import com.magikarp.android.databinding.FragmentHelpBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Class for testing {@code HelpFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
@LooperMode(PAUSED)
public class TestHelpFragment {

  @Mock
  FragmentActivity activity;

  private AutoCloseable closeable;

  private Context context;

  private FragmentHelpBinding binding;

  private HelpFragment fragment;

  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    context = ApplicationProvider.getApplicationContext();
    context.setTheme(R.style.Theme_Magikarp);
    binding = FragmentHelpBinding.inflate(LayoutInflater.from(context));
    fragment = new HelpFragment(activity, context, binding);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testDefaultConstructor() {
    new HelpFragment();

    // Confirm method completes.
  }

  @Test
  public void testPerformOnCreate() {
    final HelpFragment spy = spy(fragment);
    doReturn(activity).when(spy).requireActivity();
    doReturn(context).when(spy).requireContext();

    spy.performOnCreate();

    assertTrue(spy.hasOptionsMenu());
  }

  @Test
  public void testOnCreateOptionsMenu() {
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(anyInt(), eq(menu));
  }

  @Test
  public void testOnOptionsItemSelectedTermsOfService() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_terms_of_service);
    final HelpFragment spy = spy(fragment);
    doNothing().when(spy).startActivityFromIntent(any(Intent.class));

    assertTrue(spy.onOptionsItemSelected(item));

    verify(spy).startActivityFromIntent(any(Intent.class));
  }

  @Test
  public void testOnOptionsItemSelectedPrivacyPolicy() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_privacy_policy);
    final HelpFragment spy = spy(fragment);
    doNothing().when(spy).startActivityFromIntent(any(Intent.class));

    assertTrue(spy.onOptionsItemSelected(item));

    verify(spy).startActivityFromIntent(any(Intent.class));
  }

  @Test
  public void testOnOptionsItemSelectedUnspecified() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(Integer.MAX_VALUE);

    assertFalse(fragment.onOptionsItemSelected(item));
  }

  @Test
  public void testOnCreateView() {
    final LayoutInflater inflater = LayoutInflater.from(context);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onCreateView(inflater, null, savedInstanceState);

    assert (fragment.binding != binding);
  }

  @Test
  public void testOnViewCreated() {
    final View view = mock(View.class);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onViewCreated(view, savedInstanceState);

    assertTrue(binding.callContainer.hasOnClickListeners());
    assertTrue(binding.emailContainer.hasOnClickListeners());
  }

  @Test
  public void testOnDestroyView() {
    fragment.onDestroyView();

    assertNull(fragment.binding);
  }

  @Test
  public void testOnDestroy() {
    fragment.onDestroy();

    assertNull(fragment.activity);
    assertNull(fragment.context);
  }

  @Test
  public void testOnCallButtonClicked() {
    final View view = mock(View.class);
    final HelpFragment spy = spy(fragment);
    doNothing().when(spy).startActivityFromIntent(any(Intent.class));

    spy.onCallButtonClicked(view);

    verify(spy).startActivityFromIntent(any(Intent.class));
  }

  @Test
  public void testOnEmailButtonClicked() {
    final View view = mock(View.class);
    final HelpFragment spy = spy(fragment);
    doNothing().when(spy).startActivityFromIntent(any(Intent.class));

    spy.onEmailButtonClicked(view);

    verify(spy).startActivityFromIntent(any(Intent.class));
  }

  @Test
  public void testStartActivityFromIntent() {
    final Intent intent = mock(Intent.class);
    final ComponentName componentName = mock(ComponentName.class);
    when(intent.resolveActivity(any())).thenReturn(componentName);

    fragment.startActivityFromIntent(intent);

    verify(activity).startActivity(intent);
  }

  @Test
  public void testStartActivityFromIntentNoActivityAvailable() {
    final Intent intent = mock(Intent.class);
    when(intent.resolveActivity(any())).thenReturn(null);

    fragment.startActivityFromIntent(intent);

    verify(activity, never()).startActivity(any());
  }

}
