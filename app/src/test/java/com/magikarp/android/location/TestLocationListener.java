package com.magikarp.android.location;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.Config.OLDEST_SDK;


import android.location.Location;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.gms.location.LocationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Class for testing {@code LocationListener}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
public class TestLocationListener {

  @Test
  public void testOnLocationResult() {
    final LocationResult locationResult = mock(LocationResult.class);
    final Location location = mock(Location.class);
    when(locationResult.getLastLocation()).thenReturn(location);

    final LocationListener listener = new LocationListener();
    listener.onLocationResult(locationResult);

    assertEquals(location, listener.getLocation());
  }


}
