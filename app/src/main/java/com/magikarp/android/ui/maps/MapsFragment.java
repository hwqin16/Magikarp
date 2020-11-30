package com.magikarp.android.ui.maps;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultCallback;
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
    OnSharedPreferenceChangeListener, ActivityResultCallback<Boolean> {

  private ActivityResultLauncher<String> requestPermissionLauncher;

  private boolean isUserData;

  private GoogleMap googleMap;

  private int maxRecords;

  private MapsViewModel mapsViewModel;

  private String userId;
  @Inject
  SharedPreferences preferences;

  /**
   * Default constructor.
   */
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

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    isUserData = requireArguments().getBoolean(getString(R.string.args_is_user_data));
    setHasOptionsMenu(isUserData);
    // Set up map data repository.
    mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
    // Set up account listener (used to determine if fragment should quit while in edit mode).
    new ViewModelProvider(requireActivity()).get(GoogleSignInViewModel.class).getSignedInAccount()
        .observe(this, this::onGoogleSignInAccountChanged);
    // Set up activity to request permissions (i.e. fine location).
    requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), this);
    // Register preference listener for max records to query.
    maxRecords = Integer.parseInt(preferences
        .getString(getString(R.string.preference_key_max_records),
            getString(R.string.max_records_default)));
    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_maps, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentByTag(getString(R.string.fragment_tag_maps));
    // Map fragment should never be null (spotbugs).
    assert mapFragment != null;
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_maps, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.action_new_post) {
      // Build arguments bundle for creating a new post in the post editor.
      final Bundle args = new Bundle();
      args.putString(getString(R.string.args_post_type), getString(R.string.arg_post_type_new));
      // Launch post editor.
      NavHostFragment.findNavController(this).navigate(R.id.action_nav_maps_to_post_editor, args);
      return true;
    }
    return super.onOptionsItemSelected(item);
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
        .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      googleMap.setMyLocationEnabled(true);
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }
    googleMap.setOnCameraIdleListener(this);
    googleMap.setOnMarkerClickListener(this);
    mapsViewModel.getMessages().observe(this, this::onMessagesChanged);
  }

  @Override
  public void onActivityResult(Boolean result) {
    if (result && (googleMap != null)) {
      try {
        googleMap.setMyLocationEnabled(true);
      } catch (SecurityException unlikely) {
        googleMap.setMyLocationEnabled(false);
      }
    }
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
  @VisibleForTesting
  void onGoogleSignInAccountChanged(GoogleSignInAccount account) {
    // Quit fragment if user logs out while in edit mode.
    // Note: it should not be possible for user to change accounts without explicitly logging out.
    if (isUserData && (userId != null) && ((account == null) || !userId.equals(account.getId()))) {
      requireActivity().onBackPressed();
    } else {
      userId = (account == null) ? null : account.getId();
    }
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
    final Message message = (Message) marker.getTag();
    // Message should never be null (spotbugs).
    assert message != null;
    // Build arguments bundle for editing/viewing an existing post in the post editor.
    final Bundle bundle = new Bundle();
    bundle.putString(getString(R.string.args_post_type),
        getString(isUserData ? R.string.arg_post_type_update : R.string.arg_post_type_view));
    bundle.putParcelable(getString(R.string.args_message), message);
    // Launch post editor.
    int action =
        isUserData ? R.id.action_nav_maps_to_post_editor : R.id.action_nav_maps_to_post_viewer;
    NavHostFragment.findNavController(this).navigate(action, bundle);
    return true;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (getString(R.string.preference_key_max_records).equals(key)) {
      maxRecords = Integer
          .parseInt(sharedPreferences.getString(key, getString(R.string.max_records_default)));
    }
  }

}
