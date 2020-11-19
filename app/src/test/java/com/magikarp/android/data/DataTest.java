package com.magikarp.android.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import android.os.Parcel;
import com.android.volley.RequestQueue;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.magikarp.android.data.model.GetMessagesRequest;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.network.GsonRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Class for unit tests.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DataTest {

  @Test
  public void testMapsRepositoryGetMessagesNoUserId() {
    RequestQueue requestQueue = mock(RequestQueue.class);
    ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);
    MapsRepository.MessagesListener listener = mock(MapsRepository.MessagesListener.class);
    String url = "http://www.example.com";
    LatLngBounds bounds = new LatLngBounds(new LatLng(1, 2), new LatLng(3, 4));

    MapsRepository mapsRepository = new MapsRepository(requestQueue, url);
    mapsRepository.getMessages(null, bounds, 1, listener, null);

    verify(requestQueue).add(captor.capture());
    GsonRequest request = captor.getValue();

    GetMessagesRequest body = new GetMessagesRequest(3.0, 2.0, 1.0, 4.0, 1);
    assertEquals(request.getUrl(), url);
    assert (new String(request.getBody()).equals(new Gson().toJson(body)));
  }

  @Test
  public void testMapsRepositoryGetMessagesWithUserId() {
    RequestQueue requestQueue = mock(RequestQueue.class);
    ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);
    MapsRepository.MessagesListener listener = mock(MapsRepository.MessagesListener.class);
    String url = "http://www.example.com";
    String userId = "hello";
    LatLngBounds bounds = new LatLngBounds(new LatLng(1, 2), new LatLng(3, 4));

    MapsRepository mapsRepository = new MapsRepository(requestQueue, url);
    mapsRepository.getMessages(userId, bounds, 1, listener, null);

    verify(requestQueue).add(captor.capture());
    GsonRequest request = captor.getValue();
    GetMessagesRequest body = new GetMessagesRequest(3.0, 2.0, 1.0, 4.0, 1);
    assertEquals(request.getUrl(), url + "/" + userId);
    assert (new String(request.getBody()).equals(new Gson().toJson(body)));
  }

  @Test
  public void testGetMessagesResponseListenerOnResponseNull() {
    MapsRepository.MessagesListener messagesListener = mock(MapsRepository.MessagesListener.class);
    GetMessagesResponse response = mock(GetMessagesResponse.class);
    when(response.getMessages()).thenReturn(null);
    ArgumentCaptor<List<Message>> captor = ArgumentCaptor.forClass(List.class);

    MapsRepository.GetMessagesResponseListener listener =
        new MapsRepository.GetMessagesResponseListener(messagesListener);
    listener.onResponse(response);

    verify(messagesListener).onMessagesChanged(captor.capture());
    List<Message> request = captor.getValue();
    assertEquals(request, Collections.emptyList());
  }

  @Test
  public void testGetMessagesResponseListenerOnResponseNotNull() {
    MapsRepository.MessagesListener messagesListener = mock(MapsRepository.MessagesListener.class);
    List<Message> messageList = new ArrayList<>(1);
    GetMessagesResponse response = mock(GetMessagesResponse.class);
    when(response.getMessages()).thenReturn(messageList);
    ArgumentCaptor<List<Message>> captor = ArgumentCaptor.forClass(List.class);

    MapsRepository.GetMessagesResponseListener listener =
        new MapsRepository.GetMessagesResponseListener(messagesListener);
    listener.onResponse(response);

    verify(messagesListener).onMessagesChanged(captor.capture());
    List<Message> request = captor.getValue();
    assert (request == messageList);
  }

  @Test
  public void testMessageParcelling() {
    Parcel parcel = mock(Parcel.class);
    Message message = new Message("1", "user", "imageUrl", "text", 23, 5, "timestamp");
    message.writeToParcel(parcel, 0);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(parcel, times(5)).writeString(stringCaptor.capture());
    ArgumentCaptor<Double> doubleCaptor = ArgumentCaptor.forClass(Double.class);
    verify(parcel, times(2)).writeDouble(doubleCaptor.capture());

    List<String> capturedString = stringCaptor.getAllValues();
    assertEquals(capturedString.get(0), "1");
    assertEquals(capturedString.get(1), "user");
    assertEquals(capturedString.get(2), "imageUrl");
    assertEquals(capturedString.get(3), "text");
    assertEquals(capturedString.get(4), "timestamp");

    List<Double> capturedDouble = doubleCaptor.getAllValues();
    assertEquals(capturedDouble.get(0), Double.valueOf(23));
    assertEquals(capturedDouble.get(1), Double.valueOf(5));
  }

  @Test
  public void testMessageUnparcelling() {
    Parcel parcel = mock(Parcel.class);
    when(parcel.readString()).thenReturn("1").thenReturn("user").thenReturn("imageUrl")
        .thenReturn("text").thenReturn("timestamp");
    when(parcel.readDouble()).thenReturn(Double.valueOf(23)).thenReturn(Double.valueOf(5));

    Message message = new MessageWrapper(parcel);

    assertEquals(message.getId(), "1");
    assertEquals(message.getUserId(), "user");
    assertEquals(message.getImageUrl(), "imageUrl");
    assertEquals(message.getText(), "text");
    assertEquals(message.getTimestamp(), "timestamp");
    assertEquals(message.getLatitude(), 23, .01);
    assertEquals(message.getLongitude(), 5, .01);
  }

  /**
   * Wrapper class to help with testing.
   */
  public static class MessageWrapper extends Message {

    public MessageWrapper(Parcel in) {
      super(in);
    }

  }

}
