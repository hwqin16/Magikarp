package com.magikarp.android.network;

import androidx.annotation.Nullable;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;

public class GsonRequest<T> extends JsonRequest<T> {

  private final Class<T> clazz;

  /**
   * Create a new request.
   *
   * @param method        the HTTP method to use
   * @param url           URL to fetch the JSON from
   * @param clazz         class of object to return in response
   * @param requestBody   parameters to post with the request, or {@code null} indicating no
   *                      parameters will be posted along with request
   * @param listener      listener to receive response
   * @param errorListener listener to receive errors, or {@code null} to ignore errors
   */
  public GsonRequest(int method, String url, Class<T> clazz, @Nullable String requestBody,
                     Listener<T> listener, @Nullable ErrorListener errorListener) {
    super(method, url, requestBody, listener, errorListener);
    this.clazz = clazz;
  }

  @Override
  protected Response<T> parseNetworkResponse(NetworkResponse response) {
    try {
      final String json =
          new String(response.data, HttpHeaderParser.parseCharset(response.headers));
      return Response
          .success(new Gson().fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
    } catch (JsonSyntaxException | UnsupportedEncodingException exception) {
      return Response.error(new ParseError(exception));
    }
  }

}
