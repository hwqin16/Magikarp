package com.magikarp.android.ui.app;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.view.MenuItem;
import com.magikarp.android.R;
import org.junit.Test;

public class TestMainActivity {
  @Test
  public void testPerformOnActivityResult() {
    Intent mockIntent = mock(Intent.class);

    MainActivity mainActivity = new MainActivity();

    mainActivity.performOnActivityResult(MainActivity.SIGN_IN_RESULT - 1, mockIntent);

    verifyNoInteractions(mockIntent);
  }

  @Test
  public void testOnMenuItemClick() {
    int id = R.id.action_login - 1;
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(id);

    MainActivity mainActivity = new MainActivity();

    assertFalse(mainActivity.onMenuItemClick(mockMenuItem));
  }
}
