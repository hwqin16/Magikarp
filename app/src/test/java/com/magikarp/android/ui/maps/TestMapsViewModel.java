package com.magikarp.android.ui.maps;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code MapsViewModel}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMapsViewModel {

  @Mock
  MapsRepository mapsRepository;
  @Mock
  SavedStateHandle savedStateHandle;
  @Mock
  GetMessagesResponse getMessagesResponse;
  @Mock
  VolleyError volleyError;

  private MapsViewModel viewModel;

  @Before
  public void setup() {
    viewModel = new MapsViewModel(mapsRepository, savedStateHandle);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testGetMessages() {
    final MutableLiveData messages = new MutableLiveData();
    when(savedStateHandle.getLiveData(MapsViewModel.KEY_MESSAGES, Collections.emptyList()))
        .thenReturn(messages);

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
  public void testOnResponseMessagesNull() {
    when(getMessagesResponse.getMessages()).thenReturn(null);

    viewModel.onResponse(getMessagesResponse);

    verify(savedStateHandle).set("messages", Collections.emptyList());
  }

  @Test
  public void testOnResponseMessagesNotNull() {
    final List<Message> messages = new ArrayList<>();
    when(getMessagesResponse.getMessages()).thenReturn(messages);

    viewModel.onResponse(getMessagesResponse);

    verify(savedStateHandle).set("messages", messages);
  }

  @Test
  public void testOnErrorResponse() {
    viewModel.onErrorResponse(volleyError);

    verify(savedStateHandle).set("messages", Collections.emptyList());
  }

}
