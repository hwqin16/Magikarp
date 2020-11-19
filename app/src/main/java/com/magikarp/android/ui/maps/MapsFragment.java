package com.magikarp.android.ui.maps;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
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
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment for providing a map interface.
 */
@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback, OnCameraIdleListener,
    Observer<List<Message>>, OnMarkerClickListener {

  private static final String SAVED_STATE = "savedState";

  private boolean isUserData;

  private GoogleMap googleMap;

  private ArrayList<Message> messages;

  private MapsViewModel mapsViewModel;

  @VisibleForTesting
  MapsFragment() {
  }

  @VisibleForTesting
  MapsFragment(MapsViewModel mapsViewModel, GoogleMap googleMap) {
    this.mapsViewModel = mapsViewModel;
    this.googleMap = googleMap;
  }

  @VisibleForTesting
  MapsFragment(boolean isUserData) {
    this.isUserData = isUserData;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    performOnCreate(new ViewModelProvider(this), getString(R.string.args_is_user_data),
        savedInstanceState);
  }

  @VisibleForTesting
  void performOnCreate(
      ViewModelProvider viewModelProvider,
      String argsIsUserData,
      Bundle savedInstanceState
  ) {
    mapsViewModel = viewModelProvider.get(MapsViewModel.class);
    isUserData = requireArguments().getBoolean(argsIsUserData);
    setHasOptionsMenu(isUserData);
    // Get saved messages, if applicable.
    if (savedInstanceState != null) {
      messages = savedInstanceState.getParcelableArrayList(SAVED_STATE);
    }
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
      Resources resources = getResources();
      Bundle bundle = new Bundle();
      bundle.putBoolean(resources.getString(R.string.args_is_user_data), true);
      bundle.putDouble(resources.getString(R.string.args_latitude), Double.NaN);
      bundle.putDouble(resources.getString(R.string.args_longitude), Double.NaN);
      NavHostFragment.findNavController(this).navigate(R.id.action_nav_maps_to_post_editor, bundle);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentByTag(getResources().getString(R.string.fragment_tag_maps));
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (messages != null) {
      outState.putParcelableArrayList(SAVED_STATE, messages);
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    //googleMap.setMyLocationEnabled(true);
    googleMap.setOnCameraIdleListener(this);
    googleMap.setOnMarkerClickListener(this);
    // Load saved messages.
    if (messages != null) {
      onChanged(messages);
    }
    mapsViewModel.getMessages().observe(this, this);
  }

  @Override
  public void onCameraIdle() {
    mapsViewModel.setMapsQuery(null, googleMap.getProjection().getVisibleRegion().latLngBounds,
        20); // TODO records
  }

  @Override
  public void onChanged(@NonNull List<Message> messages) {
    // Do not update messages if there are none (ex. no network, etc.).
    if (!messages.isEmpty()) {
      this.messages = (ArrayList<Message>) messages;
      googleMap.clear();
      for (Message message : messages) {
        Marker marker = googleMap.addMarker(
            new MarkerOptions()
                .position(new LatLng(message.getLatitude(), message.getLongitude())));
        marker.setTag(message);
      }
    }
  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    Bundle bundle = new Bundle();
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

}
