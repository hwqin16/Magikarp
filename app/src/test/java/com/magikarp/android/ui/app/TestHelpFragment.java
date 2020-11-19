package com.magikarp.android.ui.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import com.magikarp.android.R;
import org.junit.Test;

public class TestHelpFragment {
  @Test
  public void testPerformOnCreate() {
    HelpFragment helpFragment = new HelpFragment();

    assertFalse(helpFragment.hasOptionsMenu());
    helpFragment.performOnCreate();
    assertTrue(helpFragment.hasOptionsMenu());
  }

  @Test
  public void testPerformOnCreateOptionsMenu() {
    Menu mockMenu = mock(Menu.class);
    MenuInflater mockMenuInflater = mock(MenuInflater.class);

    HelpFragment helpFragment = new HelpFragment();

    helpFragment.performOnCreateOptionsMenu(mockMenu, mockMenuInflater);

    verify(mockMenuInflater).inflate(R.menu.menu_help, mockMenu);
  }

  @Test
  public void testOnCreateView() {
    ViewGroup mockViewGroup = mock(ViewGroup.class);
    LayoutInflater mockLayoutInflater = mock(LayoutInflater.class);

    HelpFragment helpFragment = new HelpFragment();

    helpFragment.onCreateView(mockLayoutInflater, mockViewGroup, null);

    verify(mockLayoutInflater).inflate(R.layout.fragment_help, mockViewGroup, false);
  }
}
