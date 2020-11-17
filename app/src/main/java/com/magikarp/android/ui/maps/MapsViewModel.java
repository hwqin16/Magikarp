package com.magikarp.android.ui.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterItem;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.MapsRepository.MapClusterItemResponseListener;

import java.util.Collections;
import java.util.List;

/**
 * Class to provide map items from the map item repository.
 */
public class MapsViewModel extends ViewModel implements MapClusterItemResponseListener,
    ErrorListener {

  private static final int MAX_RECORDS = 20;

  private final MapsRepository mapsRepository;

  private final MutableLiveData<List<? extends ClusterItem>> clusterItems
      = new MutableLiveData<>();

  /**
   * Create a new map view model.
   *
   * @param mapRepository repository for accessing data
   */
  @ViewModelInject
  public MapsViewModel(@NonNull MapsRepository mapRepository) {
    this.mapsRepository = mapRepository;
  }

  @NonNull
  public LiveData<List<? extends ClusterItem>> getMapItems() {
    return clusterItems;
  }

  public void setLatLngBounds(@Nullable LatLngBounds bounds) {
    if (bounds == null) {
      clusterItems.setValue(Collections.emptyList());
    } else {
      mapsRepository.getMapItems(bounds, MAX_RECORDS, this, this);
    }
  }

  @Override
  public void onMapClusterItemResponse(List<? extends ClusterItem> messages) {
    clusterItems.setValue(messages);
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    clusterItems.setValue(Collections.emptyList());
  }

}
