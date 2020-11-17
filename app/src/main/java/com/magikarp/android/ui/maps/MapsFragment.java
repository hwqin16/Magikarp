package com.magikarp.android.ui.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
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
import java.util.List;

/**
 * A fragment for providing a map interface.
 */
@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback, OnCameraIdleListener,
    Observer<List<Message>>, OnMarkerClickListener {

  private boolean isUserData;

  // private ClusterManager<Message> clusterManager;

  private GoogleMap googleMap;

  private MapsViewModel mapsViewModel;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
    isUserData = getArguments().getBoolean(getString(R.string.args_is_user_data));
    setHasOptionsMenu(isUserData);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
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
//      NavController navController = NavHostFragment.findNavController(this);
//      return NavigationUI.onNavDestinationSelected(item, navController);
      NavDirections directions =
          MapsFragmentDirections.actionNavMapsToPostEditor(null, null, null);
      NavHostFragment.findNavController(this).navigate(directions);
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
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    //googleMap.setMyLocationEnabled(true);
    // clusterManager = new ClusterManager<>(requireContext(), googleMap);
    googleMap.setOnCameraIdleListener(this);
    googleMap.setOnMarkerClickListener(this);
    // clusterManager.setOnClusterItemClickListener();
    mapsViewModel.getMessages().observe(this, this);
  }

  @Override
  public void onCameraIdle() {
    mapsViewModel.setMapsQuery(isUserData,
        googleMap.getProjection().getVisibleRegion().latLngBounds, 20); // TODO records
  }

  @Override
  public void onChanged(List<Message> clusterItems) {
    googleMap.clear();
    for (Message message : clusterItems) {
      Marker marker = googleMap.addMarker(
          new MarkerOptions().position(new LatLng(message.getLatitude(), message.getLongitude())));
    }
  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    LatLng latLng = marker.getPosition();

    NavDirections directions;
    if (isUserData) {
      directions =
          MapsFragmentDirections.actionNavMapsToPostEditor(latLng.latitude, latLng.longitude, "");
    } else {
      directions = MapsFragmentWrapperDirections
          .actionNavMapsToPostViewer(latLng.latitude, latLng.longitude, "");
    }
    NavHostFragment.findNavController(this).navigate(directions);
    return true;
  }

}
