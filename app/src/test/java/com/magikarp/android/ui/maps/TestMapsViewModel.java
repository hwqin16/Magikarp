package com.magikarp.android.ui.maps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.model.Message;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TestMapsViewModel {

  @Mock
  MapsRepository mapsRepository;
  @Mock
  SavedStateHandle savedStateHandle;

  private MapsViewModel viewModel;

//  private final SavedStateHandle savedStateHandle;

  @Before
  public void setup() {
    viewModel = new MapsViewModel(mapsRepository, savedStateHandle);
  }

  @Test
  public void testGetMessages() {
    final MutableLiveData messages = new MutableLiveData();
    when(savedStateHandle.getLiveData("messages", Collections.emptyList())).thenReturn(messages);
    assert (viewModel.getMessages() == messages);
  }

  @Test
  public void testSetMapsQuery() {
    String userId = "testUserId";
    LatLngBounds bounds = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(1.0, 1.0));
    int maxRecords = 5;

    viewModel.setMapsQuery(userId, bounds, maxRecords);

    verify(mapsRepository).getMessages(userId, bounds, maxRecords, viewModel, viewModel);
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
