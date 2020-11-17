package com.magikarp.android.ui.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.navigation.NavigationView;
import com.magikarp.android.R;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

  private static final String TAG = "MainActivity";
  private static final int SIGN_IN_RESULT = 9001;

  private AppBarConfiguration appBarConfiguration;

  private NavController navController;

  private NavigationView navigationView;

  @Inject
  GoogleSignInClient googleSignInClient;

  @Inject
  ImageLoader imageLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    navController = ((NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment)).getNavController();
    appBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_maps, R.id.nav_my_posts, R.id.nav_settings,
            R.id.nav_help).setOpenableLayout(drawerLayout).build();

    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

    // Set up sign in buttons.
    navigationView.getMenu().findItem(R.id.action_login).setOnMenuItemClickListener(this);
    navigationView.getMenu().findItem(R.id.action_logout).setOnMenuItemClickListener(this);
    // Check for existing Google Sign In account, if the user is already signed in
    // the GoogleSignInAccount will be non-null.

    updateSignInUi(GoogleSignIn.getLastSignedInAccount(this));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SIGN_IN_RESULT) {
      try {
        GoogleSignInAccount account =
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
        updateSignInUi(account);
      } catch (ApiException e) {
        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        updateSignInUi(null);
      }
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration)
        || super.onSupportNavigateUp();
  }

  /**
   * Update UI associated with user login.
   *
   * @param account account to retrieve login information
   */
  private void updateSignInUi(@Nullable GoogleSignInAccount account) {
    Log.i(TAG, "Account: " + (account == null ? null : account.getEmail()));
//    View headerView = navigationView.getHeaderView(0);
//    TextView name = headerView.findViewById(R.id.drawer_header_name);
//    TextView email = headerView.findViewById(R.id.drawer_header_email);
//    NetworkImageView imageView = headerView.findViewById(R.id.drawer_header_image);
//    if (account != null) {
//      name.setText(account.getDisplayName());
//      email.setText(account.getEmail());
//      Uri url = account.getPhotoUrl();
//      if (url != null) {
//        String urlString = url.toString();
//        imageLoader.get(urlString, ImageLoader
//            .getImageListener(imageView, R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round));
//        imageView.setImageUrl(urlString, imageLoader);
//      }
//    } else {
//      name.setText(null);
//      email.setText(null);
//      imageView.setImageResource(R.mipmap.ic_launcher_round);
//    }
//    name.invalidate();
//    email.invalidate();
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_login) {
      Intent signInIntent = googleSignInClient.getSignInIntent();
      startActivityForResult(signInIntent, SIGN_IN_RESULT);
      return true;
    } else if (id == R.id.action_logout) {
      googleSignInClient.signOut().addOnCompleteListener(this, task -> updateSignInUi(null));
      return true;
    }
    return false;
  }

}
