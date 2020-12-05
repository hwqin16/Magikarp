package com.magikarp.android.ui.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.Config.OLDEST_SDK;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.magikarp.android.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Class for testing {@code MainActivity}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
@LooperMode(PAUSED)
public class TestMainActivity {

  @Mock
  private ActivityResultLauncher<Intent> googleSignInLauncher;
  @Mock
  private AppBarConfiguration appBarConfiguration;
  @Mock
  private DrawerLayout drawerLayout;
  @Mock
  private GoogleSignInViewModel viewModel;
  @Mock
  private NavController navController;
  @Mock
  private NavigationView navigationView;
  @Mock
  private GoogleSignInClient googleSignInClient;
  @Mock
  private ImageLoader imageLoader;

  private AutoCloseable closeable;

  private MainActivity activity;

  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    final Context context = ApplicationProvider.getApplicationContext();
    activity = new MainActivity(
        googleSignInLauncher,
        appBarConfiguration,
        context,
        drawerLayout,
        viewModel,
        navController,
        navigationView,
        googleSignInClient,
        imageLoader);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testDefaultConstructor() {
    new MainActivity();

    // Confirm method completes.
  }

  @Test
  public void testPerformOnCreate() {
    try (MockedStatic<NavigationUI> navigationUi = mockStatic(NavigationUI.class)) {
      final MainActivity spy = spy(activity);
      doNothing().when(spy).setContentView(anyInt());
      final Toolbar toolbar = mock(Toolbar.class);
      doReturn(toolbar).when(spy).findViewById(R.id.toolbar);
      doNothing().when(spy).setSupportActionBar(any());
      doReturn(drawerLayout).when(spy).findViewById(R.id.drawer_layout);
      doReturn(navigationView).when(spy).findViewById(R.id.nav_view);
      final FragmentManager fragmentManager = mock(FragmentManager.class);
      doReturn(fragmentManager).when(spy).getSupportFragmentManager();
      final NavHostFragment navHostFragment = mock(NavHostFragment.class);
      when(fragmentManager.findFragmentById(anyInt())).thenReturn(navHostFragment);
      when(navHostFragment.getNavController()).thenReturn(navController);
      final Menu menu = mock(Menu.class);
      when(navigationView.getMenu()).thenReturn(menu);
      final MenuItem item = mock(MenuItem.class);
      when(menu.findItem(anyInt())).thenReturn(item);
      final View headerView = mock(View.class);
      when(navigationView.getHeaderView(0)).thenReturn(headerView);
      final NetworkImageView imageView = mock(NetworkImageView.class);
      when(headerView.findViewById(anyInt())).thenReturn(imageView);
      doReturn(googleSignInLauncher).when(spy).registerForActivityResult(any(), any());
      doNothing().when(spy).updateSignInUi(any());

      spy.performOnCreate();

      assertNotSame(appBarConfiguration, spy.appBarConfiguration);
    }


  }

  @Test
  public void testOnMenuItemClickLogin() {
    final MenuItem item = mock(MenuItem.class);
    final Intent intent = mock(Intent.class);
    when(item.getItemId()).thenReturn(R.id.action_login);
    when(googleSignInClient.getSignInIntent()).thenReturn(intent);

    assertTrue(activity.onMenuItemClick(item));
    verify(googleSignInClient).getSignInIntent();
    verify(googleSignInLauncher).launch(intent);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testOnMenuItemClickLogout() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.action_logout);
    final Task task = mock(Task.class);
    when(googleSignInClient.signOut()).thenReturn(task);

    assertTrue(activity.onMenuItemClick(item));
    verify(googleSignInClient).signOut();
  }

  @Test
  public void testOnMenuItemClickUnspecified() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(Integer.MAX_VALUE);

    assertFalse(activity.onMenuItemClick(item));
    verifyNoInteractions(googleSignInLauncher);
    verifyNoInteractions(googleSignInClient);
  }

  @Test
  public void testOnSupportNavigateUpReturnsTrue() {
    try (MockedStatic<NavigationUI> navigationUi = mockStatic(NavigationUI.class)) {
      navigationUi.when(() -> NavigationUI.navigateUp(navController, appBarConfiguration))
          .thenReturn(true);

      assertTrue(activity.onSupportNavigateUp());
    }
  }

  @Test
  public void testOnSupportNavigateUpReturnsFalse() {
    try (MockedStatic<NavigationUI> navigationUi = mockStatic(NavigationUI.class)) {
      navigationUi.when(() -> NavigationUI.navigateUp(navController, appBarConfiguration))
          .thenReturn(false);

      assertFalse(activity.onSupportNavigateUp());
    }
  }

  @Test
  @SuppressWarnings({"rawtypes"})
  public void testOnGoogleSignInResult() throws Throwable {
    try (MockedStatic<GoogleSignIn> googleSignIn = mockStatic(GoogleSignIn.class)) {
      final ActivityResult result = mock(ActivityResult.class);
      final Intent intent = mock(Intent.class);
      final Task task = mock(Task.class);
      final GoogleSignInAccount googleSignInAccount = mock(GoogleSignInAccount.class);
      when(result.getData()).thenReturn(intent);
      when(task.getResult(ApiException.class)).thenReturn(googleSignInAccount);
      googleSignIn.when(() -> GoogleSignIn.getSignedInAccountFromIntent(intent))
          .thenReturn(task);
      final MainActivity spy = spy(activity);
      doNothing().when(spy).updateSignInUi(any());

      spy.onGoogleSignInResult(result);

      verify(spy).updateSignInUi(googleSignInAccount);
      verify(spy, never()).updateSignInUi(null);
    }
  }

  @Test
  @SuppressWarnings({"rawtypes"})
  public void testOnGoogleSignInResultThrowsApiException() throws Throwable {
    try (MockedStatic<GoogleSignIn> googleSignIn = mockStatic(GoogleSignIn.class)) {
      final ActivityResult result = mock(ActivityResult.class);
      final Intent intent = mock(Intent.class);
      final Task task = mock(Task.class);
      when(result.getData()).thenReturn(intent);
      doThrow(ApiException.class).when(task).getResult(ApiException.class);
      googleSignIn.when(() -> GoogleSignIn.getSignedInAccountFromIntent(intent))
          .thenReturn(task);
      final MainActivity spy = spy(activity);
      doNothing().when(spy).updateSignInUi(any());

      spy.onGoogleSignInResult(result);

      verify(spy).updateSignInUi(null);
    }
  }

  @Test
  public void testUpdateSignInUiAccountNotNull() {
    final String displayName = "displayName";
    final String email = "email";
    final Uri photoUrl = Uri.parse("https://www.example.com");
    final GoogleSignInAccount account = mock(GoogleSignInAccount.class);
    when(account.getDisplayName()).thenReturn(displayName);
    when(account.getEmail()).thenReturn(email);
    when(account.getPhotoUrl()).thenReturn(photoUrl);
    MainActivity spy = spy(activity);
    doNothing().when(spy).setLoggedInUi(anyString(), anyString(), any(Uri.class));
    doNothing().when(spy).setLoggedOutUi();

    spy.updateSignInUi(account);

    verify(viewModel).setAccount(account);
    verify(spy).setLoggedInUi(displayName, email, photoUrl);
    verify(spy, never()).setLoggedOutUi();
  }

  @Test
  public void testUpdateSignInUiAccountNull() {
    final MainActivity spy = spy(activity);
    doNothing().when(spy)
        .setLoggedInUi(nullable(String.class), nullable(String.class), nullable(Uri.class));
    doNothing().when(spy).setLoggedOutUi();

    spy.updateSignInUi(null);

    verify(viewModel).setAccount(null);
    verify(spy).setLoggedOutUi();
    verify(spy, never()).setLoggedInUi(any(), any(), any());
  }

  @Test
  public void testSetLoggedInUiImageUriNotNull() {
    final String displayName = "displayName";
    final String userEmail = "userEmail";
    final Uri imageUri = Uri.parse("https://www.example.com");
    final View headerView = mock(View.class);
    final TextView nameEmail = mock(TextView.class);
    final NetworkImageView imageView = mock(NetworkImageView.class);
    final Menu menu = mock(Menu.class);
    final MenuItem item = mock(MenuItem.class);
    when(navigationView.getHeaderView(anyInt())).thenReturn(headerView);
    when(navigationView.getMenu()).thenReturn(menu);
    when(menu.findItem(anyInt())).thenReturn(item);
    when(headerView.findViewById(or(eq(R.id.drawer_header_name), eq(R.id.drawer_header_email))))
        .thenReturn(nameEmail);
    when(headerView.findViewById(R.id.drawer_header_image)).thenReturn(imageView);

    activity.setLoggedInUi(displayName, userEmail, imageUri);

    final String urlString = imageUri.toString();
    verify(nameEmail).setText(displayName);
    verify(nameEmail).setText(userEmail);
    verify(imageLoader).get(eq(urlString), any(ImageLoader.ImageListener.class));
    verify(imageView).setImageUrl(urlString, imageLoader);
    verify(item, times(2)).setVisible(anyBoolean());
  }

  @Test
  public void testSetLoggedInUiImageUriNull() {
    final String displayName = "displayName";
    final String userEmail = "userEmail";
    final View headerView = mock(View.class);
    final TextView nameEmail = mock(TextView.class);
    final NetworkImageView imageView = mock(NetworkImageView.class);
    final Menu menu = mock(Menu.class);
    final MenuItem item = mock(MenuItem.class);
    when(navigationView.getHeaderView(anyInt())).thenReturn(headerView);
    when(navigationView.getMenu()).thenReturn(menu);
    when(menu.findItem(anyInt())).thenReturn(item);
    when(headerView.findViewById(or(eq(R.id.drawer_header_name), eq(R.id.drawer_header_email))))
        .thenReturn(nameEmail);
    when(headerView.findViewById(R.id.drawer_header_image)).thenReturn(imageView);

    activity.setLoggedInUi(displayName, userEmail, null);

    verify(nameEmail).setText(displayName);
    verify(nameEmail).setText(userEmail);
    verifyNoInteractions(imageLoader);
    verify(item, times(2)).setVisible(anyBoolean());
  }

  @Test
  public void testSetLoggedOutUi() {
    final View headerView = mock(View.class);
    final TextView nameEmail = mock(TextView.class);
    final NetworkImageView imageView = mock(NetworkImageView.class);
    final Menu menu = mock(Menu.class);
    final MenuItem item = mock(MenuItem.class);
    when(navigationView.getHeaderView(anyInt())).thenReturn(headerView);
    when(navigationView.getMenu()).thenReturn(menu);
    when(menu.findItem(anyInt())).thenReturn(item);
    when(headerView.findViewById(or(eq(R.id.drawer_header_name), eq(R.id.drawer_header_email))))
        .thenReturn(nameEmail);
    when(headerView.findViewById(R.id.drawer_header_image)).thenReturn(imageView);

    activity.setLoggedOutUi();

    verify(nameEmail, times(2)).setText(null);
    verify(item, times(2)).setVisible(anyBoolean());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testOnSignOutComplete() {
    final Task task = mock(Task.class);
    final MainActivity spy = spy(activity);
    doNothing().when(spy).updateSignInUi(nullable(GoogleSignInAccount.class));

    spy.onSignOutComplete(task);

    verify(spy).updateSignInUi(null);
  }

}
