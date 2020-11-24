package com.magikarp.android.ui.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.Message;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Class to provide map items from the map item repository.
 */
public class MapsViewModel extends ViewModel
    implements Response.Listener<GetMessagesResponse>, ErrorListener {

  private static final String KEY_MESSAGES = "messages";

  private final MapsRepository mapsRepository;

  private final SavedStateHandle savedStateHandle;

  /**
   * Create a new map view model.
   *
   * @param mapsRepository   repository for accessing data
   * @param savedStateHandle handle for storing and accessing saved state
   */
  @ViewModelInject
  public MapsViewModel(@NonNull MapsRepository mapsRepository,
                       @NonNull @Assisted SavedStateHandle savedStateHandle) {
    this.mapsRepository = mapsRepository;
    this.savedStateHandle = savedStateHandle;
  }

  /**
   * Get a live data object for subscribing to map item data updates.
   *
   * @return a live data object for subscribing to map item data updates
   */
  @NonNull
  public LiveData<List<Message>> getMessages() {
    return savedStateHandle.getLiveData(KEY_MESSAGES, Collections.emptyList());
  }

  /**
   * Set the query to send to the maps repository.
   *
   * @param userId     user ID
   * @param bounds     the geographic bounds of the query
   * @param maxRecords the maximum number of records to return
   */
  public void setMapsQuery(@Nullable String userId, @NonNull LatLngBounds bounds, int maxRecords) {
    mapsRepository.getMessages(userId, bounds, maxRecords, this, this);
  }

  @Override
  public void onResponse(@NotNull GetMessagesResponse response) {
    final List<Message> messages = response.getMessages();
    // Messages list from the server should not be null, but checking will ensure app doesn't crash.
    savedStateHandle.set(KEY_MESSAGES, (messages == null) ? Collections.emptyList() : messages);
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    savedStateHandle.set(KEY_MESSAGES, Collections.emptyList());
  }

}
