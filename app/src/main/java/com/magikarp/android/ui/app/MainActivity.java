package com.magikarp.android.ui.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.magikarp.android.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration appBarConfiguration;

  private NavController navController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    navController = ((NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment)).getNavController();
    appBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_maps, R.id.nav_my_posts, R.id.nav_settings,
            R.id.nav_help).setOpenableLayout(drawerLayout).build();

    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationView navigationView = findViewById(R.id.nav_view);
    NavigationUI.setupWithNavController(navigationView, navController);
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration)
        || super.onSupportNavigateUp();
  }

}
