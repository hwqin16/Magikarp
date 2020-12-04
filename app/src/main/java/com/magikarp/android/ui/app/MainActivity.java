package com.magikarp.android.ui.app;

import static com.magikarp.android.util.AssertionUtilities.require;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.magikarp.android.R;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

  @VisibleForTesting
  ActivityResultLauncher<Intent> googleSignInLauncher;
  @VisibleForTesting
  AppBarConfiguration appBarConfiguration;
  @VisibleForTesting
  Context context;
  @VisibleForTesting
  DrawerLayout drawerLayout;
  @VisibleForTesting
  GoogleSignInViewModel viewModel;
  @VisibleForTesting
  NavController navController;
  @VisibleForTesting
  NavigationView navigationView;
  @Inject
  GoogleSignInClient googleSignInClient;
  @Inject
  ImageLoader imageLoader;

  /**
   * Default constructor.
   */
  public MainActivity() {
  }

  /**
   * Constructor for testing.
   *
   * @param googleSignInLauncher test variable
   * @param appBarConfiguration  test variable
   * @param context              test variable
   * @param drawerLayout         test variable
   * @param viewModel            test variable
   * @param navController        test variable
   * @param navigationView       test variable
   * @param googleSignInClient   test variable
   * @param imageLoader          test variable
   */
  @VisibleForTesting
  MainActivity(
      ActivityResultLauncher<Intent> googleSignInLauncher,
      AppBarConfiguration appBarConfiguration,
      Context context,
      DrawerLayout drawerLayout,
      GoogleSignInViewModel viewModel,
      NavController navController,
      NavigationView navigationView,
      GoogleSignInClient googleSignInClient,
      ImageLoader imageLoader) {
    this.googleSignInLauncher = googleSignInLauncher;
    this.appBarConfiguration = appBarConfiguration;
    this.context = context;
    this.drawerLayout = drawerLayout;
    this.viewModel = viewModel;
    this.navController = navController;
    this.navigationView = navigationView;
    this.googleSignInClient = googleSignInClient;
    this.imageLoader = imageLoader;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ***** Add setup that cannot be instantiated with a unit test here. ***** //

    viewModel = new ViewModelProvider(this).get(GoogleSignInViewModel.class);
    // For unit testing.
    context = this;
    performOnCreate();
  }

  @VisibleForTesting
  void performOnCreate() {
    // Set up main activity views.
    setContentView(R.layout.activity_main);
    setSupportActionBar(findViewById(R.id.toolbar));

    // Set up navigation UI.
    drawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    NavHostFragment navHostFragment =
        (NavHostFragment) require(
            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));
    navController = navHostFragment.getNavController();
    appBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_maps, R.id.nav_my_posts,
            R.id.nav_settings, R.id.nav_help).setOpenableLayout(drawerLayout).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

    // Set up sign in buttons.
    final Menu menu = navigationView.getMenu();
    menu.findItem(R.id.action_login).setOnMenuItemClickListener(this);
    menu.findItem(R.id.action_logout).setOnMenuItemClickListener(this);

    // Set up header view for displaying user profile when signed in.
    final View headerView = navigationView.getHeaderView(0);
    final NetworkImageView imageView = headerView.findViewById(R.id.drawer_header_image);
    imageView.setDefaultImageResId(R.mipmap.ic_myplace);
    imageView.setErrorImageResId(R.mipmap.ic_myplace);

    // Set up sign in UI.
    googleSignInLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            this::onGoogleSignInResult);
    updateSignInUi(GoogleSignIn.getLastSignedInAccount(context));

    // Set the default shared preferences for the application on first run.
    PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_login) {
      googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
      drawerLayout.closeDrawers();
      return true;
    } else if (id == R.id.action_logout) {
      googleSignInClient.signOut().addOnCompleteListener(this, this::onSignOutComplete);
      drawerLayout.closeDrawers();
      return true;
    }
    return false;
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration);
  }

  @VisibleForTesting
  void onGoogleSignInResult(@NonNull ActivityResult result) {
    try {
      GoogleSignInAccount account =
          GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
      updateSignInUi(account);
    } catch (ApiException e) {
      Toast.makeText(context, context.getString(R.string.failure_sign_in), Toast.LENGTH_LONG)
          .show();
      updateSignInUi(null);
    }
  }

  /**
   * Listener for account logout.
   *
   * @param task result of logout task
   */
  @VisibleForTesting
  void onSignOutComplete(@NonNull Task<Void> task) {
    updateSignInUi(null);
  }

  /**
   * Update UI associated with user login.
   *
   * @param account account to retrieve login information
   */
  public void updateSignInUi(@Nullable GoogleSignInAccount account) {
    viewModel.setAccount(account);
    if (account != null) {
      // Set the user account text.
      setLoggedInUi(account.getDisplayName(), account.getEmail(), account.getPhotoUrl());
    } else {
      setLoggedOutUi();
    }
  }

  /**
   * Sets the logged in UI.
   *
   * @param displayName display name
   * @param userEmail   user's email
   * @param imageUri    user's profile image
   */
  @VisibleForTesting
  void setLoggedInUi(@Nullable String displayName, @Nullable String userEmail,
                     @Nullable Uri imageUri) {
    final View headerView = navigationView.getHeaderView(0);
    final TextView name = headerView.findViewById(R.id.drawer_header_name);
    final TextView email = headerView.findViewById(R.id.drawer_header_email);
    final NetworkImageView imageView = headerView.findViewById(R.id.drawer_header_image);

    name.setText(displayName);
    email.setText(userEmail);
    // Set the user account profile picture.
    if (imageUri != null) {
      final String urlString = imageUri.toString();
      imageLoader.get(urlString, ImageLoader
          .getImageListener(imageView, R.mipmap.ic_myplace, R.mipmap.ic_myplace));
      imageView.setImageUrl(urlString, imageLoader);
    }
    // Set the menu choices.
    final Menu menu = navigationView.getMenu();
    menu.findItem(R.id.action_login).setVisible(false);
    menu.findItem(R.id.nav_my_posts).setVisible(true);
    menu.setGroupEnabled(R.id.menu_group_logout, true);
    menu.setGroupVisible(R.id.menu_group_logout, true);
  }

  /**
   * Sets the logged out UI.
   */
  @VisibleForTesting
  void setLoggedOutUi() {
    final View headerView = navigationView.getHeaderView(0);
    final TextView name = headerView.findViewById(R.id.drawer_header_name);
    final TextView email = headerView.findViewById(R.id.drawer_header_email);
    final NetworkImageView imageView = headerView.findViewById(R.id.drawer_header_image);

    name.setText(null);
    email.setText(null);
    imageView.setImageUrl(null, null);
    final Menu menu = navigationView.getMenu();
    menu.findItem(R.id.action_login).setVisible(true);
    menu.findItem(R.id.nav_my_posts).setVisible(false);
    menu.setGroupEnabled(R.id.menu_group_logout, false);
    menu.setGroupVisible(R.id.menu_group_logout, false);
  }

}
