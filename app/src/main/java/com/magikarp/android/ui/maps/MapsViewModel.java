package com.magikarp.android.ui.maps;

import android.util.Log;
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

  private final MutableLiveData<List<? extends ClusterItem>> clusterItems = new MutableLiveData<>();

  /**
   * Create a new map view model.
   *
   * @param mapsRepository repository for accessing data
   */
  @ViewModelInject
  public MapsViewModel(@NonNull MapsRepository mapsRepository) {
    this.mapsRepository = mapsRepository;
  }

  /**
   * Get an live data object for subscribing to map item data updates.
   *
   * @return a live data object for subscribing to map item data updates
   */
  @NonNull
  public LiveData<List<? extends ClusterItem>> getMapItems() {
    return clusterItems;
  }

  /**
   * Set the query to send to the maps repository.
   *
   * @param isUserData {@code true} if querying for user data, {@code false} if querying for all data
   * @param bounds     the geographic bounds of the query
   */
  public void setMapsQuery(boolean isUserData, @Nullable LatLngBounds bounds) {
    if (bounds == null) {
      clusterItems.setValue(Collections.emptyList());
    } else {
      mapsRepository.getMessages(isUserData, bounds, MAX_RECORDS, this, this);
    }
  }

  @Override
  public void onMapClusterItemResponse(List<? extends ClusterItem> messages) {
    Log.i("MapsViewModel", "Response: " + messages.toString()); // TODO remove
    clusterItems.setValue(messages);
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    Log.e("MapsViewModel", "error: " + error.toString()); // TODO remove
    clusterItems.setValue(Collections.emptyList());
  }

}
