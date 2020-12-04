package com.magikarp.android.ui.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.Config.OLDEST_SDK;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.magikarp.android.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/**
 * Class for testing {@code BooleanResponseDialogFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
public class TestBooleanResponseDialogFragment {

  @Mock
  private Builder builder;

  private AutoCloseable closeable;

  private Context context;

  private BooleanResponseDialogFragment fragment;

  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    context = ApplicationProvider.getApplicationContext();
    fragment = new BooleanResponseDialogFragment(builder, context);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testDefaultConstructor() {
    new BooleanResponseDialogFragment();

    // Confirm method completes.
  }

  @Test
  public void testPerformOnCreate() {
    final BooleanResponseDialogFragment spy = spy(fragment);
    doReturn(context).when(spy).requireContext();

    spy.performOnCreate();

    verify(spy).requireContext();
  }

  @Test
  public void testOnCreateDialog() {
    final Bundle savedInstanceState = mock(Bundle.class);
    final AlertDialog dialog = mock(AlertDialog.class);
    when(builder.create()).thenReturn(dialog);

    assertNotNull(fragment.onCreateDialog(savedInstanceState));

    verify(builder).create();
  }

  @Test
  public void testOnDestroy() {
    fragment.onDestroy();

    assertNull(fragment.context);
  }

  @Test
  public void testOnPositiveButtonClick() {
    final DialogInterface dialogInterface = mock(DialogInterface.class);
    final BooleanResponseDialogFragment spy = spy(fragment);
    doNothing().when(spy).setDialogResult(anyBoolean());

    spy.onPositiveButtonClick(dialogInterface, 0);

    verify(spy).setDialogResult(true);
  }

  @Test
  public void testOnNegativeButtonClick() {
    final DialogInterface dialogInterface = mock(DialogInterface.class);
    BooleanResponseDialogFragment spy = spy(fragment);
    doNothing().when(spy).setDialogResult(anyBoolean());

    spy.onNegativeButtonClick(dialogInterface, 0);

    verify(spy).setDialogResult(false);
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testSetDialogResult() {
    final String requestKey = context.getString(R.string.dialog_result);
    BooleanResponseDialogFragment spy = spy(fragment);
    final FragmentManager fragmentManager = mock(FragmentManager.class);
    doReturn(fragmentManager).when(spy).getParentFragmentManager();

    spy.setDialogResult(true);

    verify(fragmentManager).setFragmentResult(eq(requestKey), any(Bundle.class));
  }

}
