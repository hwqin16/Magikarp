package com.magikarp.android.ui.maps;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.MapsRepository.MessagesListener;
import com.magikarp.android.data.model.Message;
import java.util.Collections;
import java.util.List;

/**
 * Class to provide map items from the map item repository.
 */
public class MapsViewModel extends ViewModel implements MessagesListener, ErrorListener {

  private final MapsRepository mapsRepository;

  private final MutableLiveData<List<Message>> liveData;

  /**
   * Create a new map view model.
   *
   * @param mapsRepository repository for accessing data
   */
  @ViewModelInject
  public MapsViewModel(@NonNull MapsRepository mapsRepository,
                       @NonNull MutableLiveData<List<Message>> liveData) {
    this.mapsRepository = mapsRepository;
    this.liveData = liveData;
  }

  /**
   * Get an live data object for subscribing to map item data updates.
   *
   * @return a live data object for subscribing to map item data updates
   */
  @NonNull
  public LiveData<List<Message>> getMessages() {
    return liveData;
  }

  /**
   * Set the query to send to the maps repository.
   *
   * @param isUserData {@code true} if querying for user data, {@code false} if querying for all data
   * @param bounds     the geographic bounds of the query
   * @param maxRecords the maximum number of records to return
   */
  public void setMapsQuery(boolean isUserData, @NonNull LatLngBounds bounds, int maxRecords) {
    mapsRepository.getMessages(isUserData, bounds, maxRecords, this, this);
  }

  @Override
  public void onMessagesChanged(List<Message> messages) {
    liveData.setValue(messages);
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    liveData.setValue(Collections.emptyList());
  }

}
