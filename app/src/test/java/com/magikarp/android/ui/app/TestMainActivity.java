package com.magikarp.android.ui.app;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.material.navigation.NavigationView;
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

  @Test
  public void testUpdateSignInUi() {
    TextView mockName = mock(TextView.class);
    TextView mockEmail = mock(TextView.class);
    NetworkImageView mockImageView = mock(NetworkImageView.class);
    View mockHeaderView = mock(View.class);
    when(mockHeaderView.findViewById(R.id.drawer_header_name)).thenReturn(mockName);
    when(mockHeaderView.findViewById(R.id.drawer_header_email)).thenReturn(mockEmail);
    when(mockHeaderView.findViewById(R.id.drawer_header_image)).thenReturn(mockImageView);

    MenuItem mockLoginItem = mock(MenuItem.class);
    MenuItem mockNavItem = mock(MenuItem.class);
    Menu mockMenu = mock(Menu.class);
    when(mockMenu.findItem(R.id.action_login)).thenReturn(mockLoginItem);
    when(mockMenu.findItem(R.id.nav_my_posts)).thenReturn(mockNavItem);

    NavigationView mockNavigationView = mock(NavigationView.class);
    when(mockNavigationView.getHeaderView(0)).thenReturn(mockHeaderView);
    when(mockNavigationView.getMenu()).thenReturn(mockMenu);

    MainActivity mainActivity = new MainActivity(mockNavigationView);

    mainActivity.updateSignInUi(null);

    verify(mockName).setText(null);
    verify(mockEmail).setText(null);
    verify(mockImageView).setImageUrl(null, null);
    verify(mockLoginItem).setVisible(true);
    verify(mockNavItem).setVisible(false);
    verify(mockMenu).setGroupEnabled(R.id.menu_group_logout, false);
    verify(mockMenu).setGroupVisible(R.id.menu_group_logout, false);
  }
}
