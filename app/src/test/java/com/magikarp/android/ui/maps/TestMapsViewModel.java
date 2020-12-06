package com.magikarp.android.ui.maps;

import static com.magikarp.android.ui.maps.MapsViewModel.KEY_MESSAGES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.android.volley.Response.ErrorListener;
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
    when(savedStateHandle.getLiveData(KEY_MESSAGES, Collections.emptyList()))
        .thenReturn(messages);

    assert (viewModel.getMessages() == messages);
  }

  @Test
  public void testSetMapsQuery() {
    final String userId = "testUserId";
    LatLngBounds bounds = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(1.0, 1.0));
    int maxRecords = 5;
    final ErrorListener errorListener = mock(ErrorListener.class);

    viewModel.setMapsQuery(userId, bounds, maxRecords, errorListener);

    verify(mapsRepository).getMessages(userId, bounds, maxRecords, viewModel, errorListener);
  }

  @Test
  public void testOnResponseMessagesNull() {
    when(getMessagesResponse.getMessages()).thenReturn(null);

    viewModel.onResponse(getMessagesResponse);

    verify(savedStateHandle, never()).set(any(), any());
  }

  @Test
  public void testOnResponseMessagesEmpty() {
    final List<Message> messages = new ArrayList<>();
    when(getMessagesResponse.getMessages()).thenReturn(messages);

    viewModel.onResponse(getMessagesResponse);

    verify(savedStateHandle, never()).set(any(), any());
  }

  @Test
  public void testOnResponseMessagesWithItems() {
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    final List<Message> messages = new ArrayList<>(3);
    messages.add(message);
    messages.add(message);
    messages.add(message);
    when(getMessagesResponse.getMessages()).thenReturn(messages);

    viewModel.onResponse(getMessagesResponse);

    verify(savedStateHandle).set(KEY_MESSAGES, messages);
  }

}
