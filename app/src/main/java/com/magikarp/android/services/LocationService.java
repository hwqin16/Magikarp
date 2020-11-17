package com.magikarp.android.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import java.util.concurrent.TimeUnit;

/**
 * Service used to fetch location from GPS or Network.
 */
public class LocationService extends Service implements LocationListener {
  private LocationManager locationManager;
  private boolean isLocationEnabled;
  private static final long MINIMUM_TIME_BETWEEN_UPDATES = TimeUnit.SECONDS.toMillis(1);
  private static final long MINIMUM_DISTANCE_BETWEEN_UPDATES_METERS = 10;
  private Location location;
  private LocationServiceBinder binder = new LocationServiceBinder();

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    final int result = super.onStartCommand(intent, flags, startId);
    try {
      locationManager = (LocationManager) getApplication().getApplicationContext()
          .getSystemService(LOCATION_SERVICE);
      boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      boolean isNetworkEnabled =
          locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      Log.e("NTC", "GPS " + Boolean.toString(isGpsEnabled));
      if (isGpsEnabled || isNetworkEnabled) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

          // TODO: Request permission
          Log.d("LocationService", "Failed to initialize location service.  Requires permissions.");
        } else {
          isLocationEnabled = true;

          if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_BETWEEN_UPDATES_METERS, this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
          }

          if (isGpsEnabled) {
            locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES,
                    MINIMUM_DISTANCE_BETWEEN_UPDATES_METERS, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
          }
        }
      }
    } catch (final Exception e) {
      Log.e("LocationService", "Failed to initialize location service.", e);
    }

    return result;
  }

  /**
   * Gets the last known location of the device.
   *
   * @return last location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Checks if location services are enabled.
   *
   * @return are location services enabled
   */
  public boolean isLocationEnabled() {
    return isLocationEnabled;
  }

  /**
   * Disposes of the service.
   */
  public void dispose() {
    locationManager.removeUpdates(this);
  }

  /**
   * Binder for the service.
   */
  public class LocationServiceBinder extends Binder {
    /**
     * Gets the location service from the binder.
     *
     * @return location service
     */
    public LocationService getService() {
      return LocationService.this;
    }
  }

  @Override
  public void onLocationChanged(@NonNull Location location) {
    this.location = location;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
}
