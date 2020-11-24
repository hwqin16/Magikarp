package com.magikarp.android.ui.maps;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.magikarp.android.R;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.ui.app.GoogleSignInViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import javax.inject.Inject;

/**
 * A fragment for providing a map interface.
 */
@AndroidEntryPoint
public class MapsFragment extends Fragment
    implements OnMapReadyCallback, OnCameraIdleListener, OnMarkerClickListener,
    OnSharedPreferenceChangeListener {

  private boolean isUserData;

  private GoogleMap googleMap;

  private int maxRecords;

  private MapsViewModel mapsViewModel;

  private String userId;
  @Inject
  FusedLocationProviderClient fusedLocationClient;
  @Inject
  LocationRequest locationRequest;
  @Inject
  SharedPreferences preferences;

  @VisibleForTesting
  public MapsFragment() {
  }

  /**
   * MapsFragment constructor for testing.
   *
   * @param mapsViewModel MapsViewModel to set
   * @param googleMap     GoogleMap to set
   * @param preferences   SharedPreferences to set
   * @param isUserData    boolean to set
   * @param maxRecords    int to set
   */
  @VisibleForTesting
  MapsFragment(MapsViewModel mapsViewModel, GoogleMap googleMap, SharedPreferences preferences,
               boolean isUserData, int maxRecords) {
    this.mapsViewModel = mapsViewModel;
    this.googleMap = googleMap;
    this.preferences = preferences;
    this.isUserData = isUserData;
    this.maxRecords = maxRecords;
  }

  @VisibleForTesting
  MapsFragment(boolean isUserData) {
    this.isUserData = isUserData;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    performOnCreate(new ViewModelProvider(this), getString(R.string.args_is_user_data),
        getString(R.string.max_records), getString(R.string.max_records_default));
  }

  @VisibleForTesting
  void performOnCreate(ViewModelProvider viewModelProvider, String argsIsUserData,
                       String maxRecords, String maxRecordsDefault) {
    isUserData = requireArguments().getBoolean(argsIsUserData);
    mapsViewModel = viewModelProvider.get(MapsViewModel.class);
    new ViewModelProvider(requireActivity()).get(GoogleSignInViewModel.class)
        .getSignedInAccount()
        .observe(this, this::onGoogleSignInAccountChanged);
    setHasOptionsMenu(requireArguments().getBoolean(argsIsUserData));
    // Register preference listener to get max records to query.
    this.maxRecords = Integer.parseInt(preferences.getString(maxRecords, maxRecordsDefault));
    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    performOnCreateOptionsMenu(menu, inflater);
  }

  @VisibleForTesting
  void performOnCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_maps, menu);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_maps, container, false);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.nav_post_editor) {
      final Bundle args = new Bundle();
      args.putBoolean(getString(R.string.args_is_user_data), true);
      args.putDouble(getString(R.string.args_latitude), Double.NaN);
      args.putDouble(getString(R.string.args_longitude), Double.NaN);
      NavHostFragment.findNavController(this).navigate(R.id.action_nav_maps_to_post_editor, args);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentByTag(getResources().getString(R.string.fragment_tag_maps));
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // Unregister preference listener.
    preferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    if (ActivityCompat
        .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
      googleMap.setMyLocationEnabled(true);
    } else {
      final ActivityResultLauncher<String> requestPermissionLauncher =
          requireActivity()
              .registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                  isGranted -> {
                    if (isGranted && (googleMap != null)) {
                      try {
                        googleMap.setMyLocationEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                      } catch (SecurityException unlikely) {
                        googleMap.setMyLocationEnabled(false);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                      }
                    }
                  });
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }
    googleMap.setOnCameraIdleListener(this);
    googleMap.setOnMarkerClickListener(this);
    mapsViewModel.getMessages().observe(this, this::onMessagesChanged);
  }

  @Override
  public void onCameraIdle() {
    final String id = (isUserData) ? userId : null;
    mapsViewModel.setMapsQuery(id, googleMap.getProjection().getVisibleRegion().latLngBounds,
        maxRecords);
  }

  /**
   * Callback for Google Sign-In account changes.
   *
   * @param account the current signed-in account
   */
  void onGoogleSignInAccountChanged(GoogleSignInAccount account) {
    userId = (account == null) ? null : account.getId(); // TODO handle activity state
  }

  /**
   * Callback for messages changes.
   *
   * @param messages the current list of messages
   */
  void onMessagesChanged(List<Message> messages) {
    // Do not update messages if there are none (ex. no network, etc.).
    if ((googleMap != null) && (messages != null) && !messages.isEmpty()) {
      googleMap.clear();
      for (Message message : messages) {
        final Marker marker = googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(message.getLatitude(), message.getLongitude())));
        marker.setTag(message);
      }
    }
  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    final Bundle bundle = new Bundle();
    prepareBundleFromMarker(bundle, marker, getResources());
    int action =
        isUserData ? R.id.action_nav_maps_to_post_editor : R.id.action_nav_maps_to_post_viewer;
    NavHostFragment.findNavController(this).navigate(action, bundle);
    return true;
  }

  @VisibleForTesting
  Bundle prepareBundleFromMarker(Bundle bundle, Marker marker, Resources resources) {
    LatLng latLng = marker.getPosition();
    Message message = (Message) marker.getTag();
    assert message != null;

    bundle.putBoolean(resources.getString(R.string.args_is_user_data), isUserData);
    bundle.putDouble(resources.getString(R.string.args_latitude), latLng.latitude);
    bundle.putDouble(resources.getString(R.string.args_longitude), latLng.longitude);
    bundle.putString(resources.getString(R.string.args_text), message.getText());
    bundle.putString(resources.getString(R.string.args_image_uri), message.getImageUrl());
    return bundle;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    maxRecords = Integer.parseInt(preferences.getString(getString(R.string.max_records), "20"));
  }

  @VisibleForTesting
  String getUserId() {
    return this.userId;
  }

  @VisibleForTesting
  int getMaxRecords() {
    return this.maxRecords;
  }

}
