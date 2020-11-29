package com.magikarp.android.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import android.os.Parcel;
import com.magikarp.android.data.model.Message;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code Message}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMessage {

  private final String id = "id";

  private final String userId = "userId";

  private final String imageUrl = "imageUrl";

  private final String text = "text";

  private final double latitude = 37.8d;

  private final double longitude = -122.4d;

  private final String timestamp = "timestamp";
  @Mock
  Parcel parcel;

  @Test
  public void testMessageParcelling() {
    final Message message = new Message(id, userId, imageUrl, text, latitude, longitude, timestamp);

    message.writeToParcel(parcel, 0);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(parcel, times(5)).writeString(stringCaptor.capture());
    ArgumentCaptor<Double> doubleCaptor = ArgumentCaptor.forClass(Double.class);
    verify(parcel, times(2)).writeDouble(doubleCaptor.capture());

    List<String> capturedString = stringCaptor.getAllValues();
    assertEquals(capturedString.get(0), id);
    assertEquals(capturedString.get(1), userId);
    assertEquals(capturedString.get(2), imageUrl);
    assertEquals(capturedString.get(3), text);
    assertEquals(capturedString.get(4), timestamp);

    List<Double> capturedDouble = doubleCaptor.getAllValues();
    assertEquals(capturedDouble.get(0), Double.valueOf(latitude));
    assertEquals(capturedDouble.get(1), Double.valueOf(longitude));
  }

  @Test
  public void testMessageUnparcelling() {
    when(parcel.readString()).thenReturn(id).thenReturn(userId).thenReturn(imageUrl)
        .thenReturn(text).thenReturn(timestamp);
    when(parcel.readDouble()).thenReturn(latitude).thenReturn(longitude);

    final Message message = Message.CREATOR.createFromParcel(parcel);

    assertEquals(message.getId(), id);
    assertEquals(message.getUserId(), userId);
    assertEquals(message.getImageUrl(), imageUrl);
    assertEquals(message.getText(), text);
    assertEquals(message.getTimestamp(), timestamp);
    assertEquals(message.getLatitude(), latitude, .01);
    assertEquals(message.getLongitude(), longitude, .01);
  }

}
