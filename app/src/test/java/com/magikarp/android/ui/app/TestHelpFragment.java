package com.magikarp.android.ui.app;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;

/**
 * Class for testing {@code HelpFragment}.
 */
public class TestHelpFragment {

  private HelpFragment fragment;

  @Before
  public void setup() {
    fragment = new HelpFragment();
  }

  @Test
  public void testOnCreateOptionsMenu() {
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(anyInt(), eq(menu));
  }

  @Test
  public void testOnCreateView() {
    final LayoutInflater inflater = mock(LayoutInflater.class);
    final ViewGroup container = mock(ViewGroup.class);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onCreateView(inflater, container, savedInstanceState);

    verify(inflater).inflate(anyInt(), eq(container), eq(false));
  }

}
