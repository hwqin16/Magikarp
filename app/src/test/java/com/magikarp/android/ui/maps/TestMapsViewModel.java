package com.magikarp.android.ui.maps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.lifecycle.MutableLiveData;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.model.Message;
import java.util.Collections;
import org.junit.Test;

public class TestMapsViewModel {
  @Test
  public void testSetMapsQuery() {
    String userId = "testUserId";
    LatLngBounds bounds = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(1.0, 1.0));
    int maxRecords = 5;

    MutableLiveData messagesLiveData = mock(MutableLiveData.class);
    MapsRepository mockMapsRepository = mock(MapsRepository.class);

    MapsViewModel mapsViewModel = new MapsViewModel(mockMapsRepository, messagesLiveData);

    mapsViewModel.setMapsQuery(userId, bounds, maxRecords);

    verify(mockMapsRepository)
        .getMessages(userId, bounds, maxRecords, mapsViewModel, mapsViewModel);
  }

  @Test
  public void testOnMessagesChanged() {
    MapsRepository mockMapsRepository = mock(MapsRepository.class);
    MutableLiveData mockLiveData = mock(MutableLiveData.class);
    Message mockMessage = mock(Message.class);

    MapsViewModel mapsViewModel = new MapsViewModel(mockMapsRepository, mockLiveData);

    mapsViewModel.onMessagesChanged(Collections.singletonList(mockMessage));

    verify(mockLiveData).setValue(Collections.singletonList(mockMessage));
  }

  @Test
  public void testOnErrorResponse() {
    VolleyError volleyError = mock(VolleyError.class);
    MapsRepository mockMapsRepository = mock(MapsRepository.class);
    MutableLiveData mockLiveData = mock(MutableLiveData.class);

    MapsViewModel mapsViewModel = new MapsViewModel(mockMapsRepository, mockLiveData);

    mapsViewModel.onErrorResponse(volleyError);

    verify(mockLiveData).setValue(Collections.EMPTY_LIST);
  }
}
