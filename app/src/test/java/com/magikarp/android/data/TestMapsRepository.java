package com.magikarp.android.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.magikarp.android.data.model.GetMessagesRequest;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.network.GsonRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code MapsRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMapsRepository {

  private final String urlGetMessages = "https://www.example.com";

  private final String urlGetUserMessages = "https://www.example.com/%s";

  @Mock
  RequestQueue requestQueue;
  @Mock
  Response.Listener<GetMessagesResponse> listener;

  private MapsRepository mapsRepository;

  @Before
  public void setup() {
    mapsRepository = new MapsRepository(requestQueue, urlGetMessages, urlGetUserMessages);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testGetMessagesNoUserId() {
    final ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);
    final LatLngBounds bounds = new LatLngBounds(new LatLng(1, 2), new LatLng(3, 4));

    mapsRepository.getMessages(null, bounds, 1, listener, null);

    verify(requestQueue).add(captor.capture());
    GsonRequest request = captor.getValue();

    GetMessagesRequest body = new GetMessagesRequest(3.0, 2.0, 1.0, 4.0, 1);
    assertEquals(request.getUrl(), urlGetMessages);
    assertEquals(new String(request.getBody()), new Gson().toJson(body));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testGetMessagesWithUserId() {
    final String userId = "userId";
    ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);
    LatLngBounds bounds = new LatLngBounds(new LatLng(1, 2), new LatLng(3, 4));

    mapsRepository.getMessages(userId, bounds, 1, listener, null);

    verify(requestQueue).add(captor.capture());
    GsonRequest<GetMessagesResponse> request = captor.getValue();

    GetMessagesRequest body = new GetMessagesRequest(3.0, 2.0, 1.0, 4.0, 1);
    assertEquals(request.getUrl(), String.format(urlGetUserMessages, userId));
    assertEquals(new String(request.getBody()), new Gson().toJson(body));
  }

}
