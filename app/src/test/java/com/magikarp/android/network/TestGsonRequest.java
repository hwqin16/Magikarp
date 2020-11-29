package com.magikarp.android.network;

import static org.junit.Assert.assertEquals;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.magikarp.android.data.model.NewMessageResponse;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code GsonRequest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGsonRequest {

  private static final String url = "https://www.example.com";

  @Mock
  ErrorListener errorListener;
  @Mock
  Listener<NewMessageResponse> listener;

  private GsonRequest<NewMessageResponse> request;

  @Before
  public void setup() {
    request =
        new GsonRequest<>(Request.Method.GET, url, NewMessageResponse.class, null, listener,
            errorListener);
  }

  @Test
  public void testGsonRequestParseNetworkResponse() {
    NetworkResponse networkResponse =
        new NetworkResponse(201, "{\"record_id\":1}".getBytes(), false, 123, new ArrayList<>());

    final Response<NewMessageResponse> response = request.parseNetworkResponse(networkResponse);

    assertEquals(response.result.getRecordId(), "1");
  }

}
