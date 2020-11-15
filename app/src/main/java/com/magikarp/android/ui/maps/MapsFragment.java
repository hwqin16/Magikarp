package com.magikarp.android.ui.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.magikarp.android.R;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment for providing a map interface.
 */
@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback, OnCameraIdleListener,
        Observer<List<? extends ClusterItem>> {

    private GoogleMap googleMap;

    private MapsViewModel mapsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
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
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentByTag(getResources().getString(R.string.fragment_tag_maps));
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mapsViewModel.getMapItems().observe(this, this);
    }

    @Override
    public void onCameraIdle() {
        mapsViewModel.setLatLngBounds(googleMap.getProjection().getVisibleRegion().latLngBounds);
    }

    @Override
    public void onChanged(List<? extends ClusterItem> clusterItems) {
        googleMap.clear();
        for (ClusterItem item : clusterItems) {
            googleMap.addMarker(new MarkerOptions().position(item.getPosition())
                    .title(item.getTitle()));
        }
    }

}
