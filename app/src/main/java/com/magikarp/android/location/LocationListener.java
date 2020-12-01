package com.magikarp.android.location;

import android.location.Location;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

/**
 * Listener for location updates.
 */
public class LocationListener extends LocationCallback {

  private Location location;

  @Override
  public void onLocationResult(LocationResult locationResult) {
    location = locationResult.getLastLocation();
  }

  public Location getLocation() {
    return location;
  }

}
