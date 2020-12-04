package com.magikarp.android.ui.maps;

import static com.magikarp.android.util.AssertionUtilities.require;

import android.Manifest;
import android.content.Context;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
    OnSharedPreferenceChangeListener {

  @VisibleForTesting
  ActivityResultLauncher<String> requestPermissionLauncher;
  @VisibleForTesting
  Bundle arguments;
  @VisibleForTesting
  Context context;
  @VisibleForTesting
  FragmentActivity activity;
  @VisibleForTesting
  GoogleMap googleMap;
  @VisibleForTesting
  GoogleSignInAccount googleSignInAccount;
  @VisibleForTesting
  int maxRecords;
  @VisibleForTesting
  MapsViewModel mapsViewModel;
  @Inject
  SharedPreferences preferences;

  /**
   * Default constructor.
   */
  public MapsFragment() {
  }

  /**
   * Constructor for testing.
   *
   * @param requestPermissionLauncher test variable
   * @param arguments                 test variable
   * @param context                   test variable
   * @param activity                  test variable
   * @param googleMap                 test variable
   * @param googleSignInAccount       test variable
   * @param mapsViewModel             test variable
   * @param preferences               test variable
   */
  @VisibleForTesting
  MapsFragment(
      ActivityResultLauncher<String> requestPermissionLauncher,
      Bundle arguments,
      Context context,
      FragmentActivity activity,
      GoogleMap googleMap,
      GoogleSignInAccount googleSignInAccount,
      MapsViewModel mapsViewModel,
      SharedPreferences preferences) {
    this.requestPermissionLauncher = requestPermissionLauncher;
    this.arguments = arguments;
    this.context = context;
    this.activity = activity;
    this.googleMap = googleMap;
    this.googleSignInAccount = googleSignInAccount;
    this.mapsViewModel = mapsViewModel;
    this.preferences = preferences;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ***** Add setup that cannot be instantiated with a unit test here. ***** //

    // Set up map data repository.
    mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
    // Set up account listener (used to determine if fragment should quit while in edit mode).
    new ViewModelProvider(requireActivity()).get(GoogleSignInViewModel.class).getSignedInAccount()
        .observe(this, this::onGoogleSignInAccountChanged);
    performOnCreate();
  }

  @VisibleForTesting
  void performOnCreate() {
    // For unit testing.
    activity = requireActivity();
    arguments = requireArguments();
    context = requireContext();
    setHasOptionsMenu(requireArguments().getBoolean(context.getString(R.string.args_is_user_data)));
    // Set up activity to request permissions (i.e. fine location).
    requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            this::onRequestPermissionResult);
    // Register preference listener for max records to query.
    maxRecords = Integer.parseInt(preferences
        .getString(context.getString(R.string.preference_key_max_records),
            context.getString(R.string.max_records_default)));
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
        .findFragmentByTag(context.getString(R.string.fragment_tag_maps));
    require(mapFragment).getMapAsync(this);
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
      args.putString(context.getString(R.string.args_post_type),
          context.getString(R.string.arg_post_type_new));
      // Launch post editor.
      NavHostFragment.findNavController(this).navigate(R.id.action_nav_maps_to_post_editor, args);
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    activity = null;
    arguments = null;
    context = null;
    // Unregister preference listener.
    preferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    this.googleMap = googleMap;
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      googleMap.setMyLocationEnabled(true);
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }
    googleMap.setOnCameraIdleListener(this);
    googleMap.setOnMarkerClickListener(this);
    mapsViewModel.getMessages().observe(this, this::onMessagesChanged);
    // Load initial messages now.
    onCameraIdle();
  }

  /**
   * The result of a "request permission" request.
   *
   * @param result {@code true} if permission granted, {@code false} otherwise
   */
  @VisibleForTesting
  void onRequestPermissionResult(Boolean result) {
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
    final boolean isUserData = arguments.getBoolean(context.getString(R.string.args_is_user_data));
    final String id = (isUserData) ? googleSignInAccount.getId() : null;
    mapsViewModel.setMapsQuery(id, googleMap.getProjection().getVisibleRegion().latLngBounds,
        maxRecords);
  }

  /**
   * Callback for Google Sign-In account changes.
   *
   * @param account the current signed-in account
   */
  @VisibleForTesting
  void onGoogleSignInAccountChanged(@Nullable GoogleSignInAccount account) {
    String userId = null;
    if (googleSignInAccount != null) {
      userId = require(googleSignInAccount.getId());
    }
    // Quit fragment if user logs out while in edit mode.
    // Note: it should not be possible for user to change accounts without explicitly logging out.
    final boolean isUserData = arguments.getBoolean(context.getString(R.string.args_is_user_data));
    final boolean shouldLogOut =
        isUserData && (userId != null) && ((account == null) || !userId.equals(account.getId()));
    if (shouldLogOut) {
      activity.onBackPressed();
    } else {
      googleSignInAccount = account;
    }
  }

  /**
   * Callback for messages changes.
   *
   * @param messages the current list of messages
   */
  @VisibleForTesting
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
    final boolean isUserData = arguments.getBoolean(context.getString(R.string.args_is_user_data));
    final Message message = require((Message) marker.getTag());
    // Build arguments bundle for editing/viewing an existing post in the post editor.
    final Bundle bundle = new Bundle();
    bundle.putString(context.getString(R.string.args_post_type), context
        .getString(isUserData ? R.string.arg_post_type_update : R.string.arg_post_type_view));
    bundle.putParcelable(context.getString(R.string.args_message), message);
    // Launch post editor.
    int action =
        isUserData ? R.id.action_nav_maps_to_post_editor : R.id.action_nav_maps_to_post_viewer;
    NavHostFragment.findNavController(this).navigate(action, bundle);
    return true;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (context.getString(R.string.preference_key_max_records).equals(key)) {
      maxRecords = Integer.parseInt(
          sharedPreferences.getString(key, context.getString(R.string.max_records_default)));
    }
  }

}
