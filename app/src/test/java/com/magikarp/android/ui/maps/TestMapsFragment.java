package com.magikarp.android.ui.maps;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.magikarp.android.R;
import com.magikarp.android.data.model.Message;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code MapsFragment}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMapsFragment {

  private GoogleMap googleMap;

  private MapsViewModel mapsViewModel;

  private SharedPreferences preferences;

  private MapsFragment fragment;

  @Before
  public void setup() {
    fragment = new MapsFragment();
    googleMap = mock(GoogleMap.class);
    mapsViewModel = mock(MapsViewModel.class);
    preferences = mock(SharedPreferences.class);
  }

//    int maxRecords = 10;
//    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);
//    ViewModelProvider mockViewModelProvider = mock(ViewModelProvider.class);
//    when(mockViewModelProvider.get(MapsViewModel.class)).thenReturn(mockMapsViewModel);
//    SharedPreferences mockSharedPreferences = mock(SharedPreferences.class);
//    when(mockSharedPreferences.getString(any(), any())).thenReturn(Integer.toString(maxRecords));
//
//    boolean hasOptionsMenu = true;
//    String argsIsUserData = "arrrg";
//    Bundle mockArguments = mock(Bundle.class);
//    when(mockArguments.getBoolean(argsIsUserData)).thenReturn(hasOptionsMenu);
//    MapsFragment mapsFragment = new MapsFragment(
//        null,
//        null,
//        mockSharedPreferences,
//        false,
//        0
//    );
//    mapsFragment.setArguments(mockArguments);
//
//    mapsFragment.performOnCreate(mockViewModelProvider, argsIsUserData, "any", null);
//
//    assertTrue(mapsFragment.hasOptionsMenu());
//    assertEquals(maxRecords, mapsFragment.getMaxRecords());
//  }

  @Test
  public void testOnCreateView() {
    final LayoutInflater inflater = mock(LayoutInflater.class);
    final ViewGroup container = mock(ViewGroup.class);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onCreateView(inflater, container, savedInstanceState);

    verify(inflater).inflate(R.layout.fragment_maps, container, false);
  }


  @Test
  public void testOnCreateOptionsMenu() {
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(R.menu.menu_maps, menu);
  }

  @Test
  public void testOnOptionsItemSelectedInvalidItem() {
    int id = R.id.nav_post_editor - 1;
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(id);

    assertFalse(fragment.onOptionsItemSelected(item));
  }

  @Test
  public void testOnDestroy() {
    final MapsFragment mockFragment = new MapsFragment(mapsViewModel, googleMap, preferences,
        true, 20);

    mockFragment.onDestroy();

    verify(preferences).unregisterOnSharedPreferenceChangeListener(mockFragment);
  }

//  @Test
//  public void testOnSharedPreferenceChangedMaxRecords() {
//    final String key = context.getString(R.string.max_records);
//    final String value = context.getString(R.string.max_records_default);
//    final int maxRecords = 100;
//    SharedPreferences preferences = mock(SharedPreferences.class);
//    when(preferences.getString(key, value)).thenReturn(Integer.toString(maxRecords));
//
//    fragment.onSharedPreferenceChanged(preferences, key);
//
//    assertEquals(fragment.maxRecords, maxRecords);
//  }

//  @Test
//  public void testPerformOnMapReady() {
//    GoogleMap mockGoogleMap = mock(GoogleMap.class);
//    LiveData mockLiveData = mock(LiveData.class);
//    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);
//    when(mockMapsViewModel.getMessages()).thenReturn(mockLiveData);
//    String userId = "user123";
//    GoogleSignInAccount mockAccount = mock(GoogleSignInAccount.class);
//    when(mockAccount.getId()).thenReturn(userId);
//
//    MapsFragment mapsFragment = new MapsFragment(mockMapsViewModel, null, null, true, 0);
//
//    mapsFragment.performOnMapReady(mockGoogleMap, mockAccount);
//
//    verify(mockGoogleMap).setOnCameraIdleListener(mapsFragment);
//    verify(mockGoogleMap).setOnMarkerClickListener(mapsFragment);
//    verify(mockLiveData).observe(mapsFragment, mapsFragment);
//    assertEquals(userId, mapsFragment.getUserId());
//  }

  @Test
  public void testOnActivityResultTrue() {
    final MapsFragment fragment = new MapsFragment(mapsViewModel, googleMap, preferences, false, 0);

    fragment.onActivityResult(true);

    verify(googleMap, times(1)).setMyLocationEnabled(true);
    verify(googleMap, never()).setMyLocationEnabled(false);
  }

  @Test
  public void testOnActivityResultFalse() {
    final MapsFragment fragment = new MapsFragment(mapsViewModel, googleMap, preferences, false, 0);

    fragment.onActivityResult(false);

    verify(googleMap, never()).setMyLocationEnabled(true);
    verify(googleMap, never()).setMyLocationEnabled(false);
  }

  @Test
  public void testOnActivityResultNoMap() {
    final MapsFragment fragment = new MapsFragment(mapsViewModel, null, preferences, false, 0);

    fragment.onActivityResult(false);

    verify(googleMap, never()).setMyLocationEnabled(true);
    verify(googleMap, never()).setMyLocationEnabled(false);
  }

  @Test
  public void testOnActivityResultSecurityException() {
    doThrow(new SecurityException()).when(googleMap).setMyLocationEnabled(true);
    final MapsFragment fragment = new MapsFragment(mapsViewModel, googleMap, preferences, false, 0);

    fragment.onActivityResult(true);

    verify(googleMap, times(1)).setMyLocationEnabled(true);
    verify(googleMap, times(1)).setMyLocationEnabled(false);
  }

  @Test
  public void testOnCameraIdle() {
    int maxRecords = 10;
    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(1.0, 1.0));
    VisibleRegion visibleRegion = new VisibleRegion(null, null, null, null, latLngBounds);
    Projection mockProjection = mock(Projection.class);
    when(mockProjection.getVisibleRegion()).thenReturn(visibleRegion);
    GoogleMap mockGoogleMap = mock(GoogleMap.class);
    when(mockGoogleMap.getProjection()).thenReturn(mockProjection);
    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);

    MapsFragment mapsFragment = new MapsFragment(
        mockMapsViewModel,
        mockGoogleMap,
        null,
        false,
        maxRecords
    );

    mapsFragment.onCameraIdle();

    verify(mockMapsViewModel).setMapsQuery(null, latLngBounds, maxRecords);
  }

  @Test
  public void testOnChanged() {
    Message mockMessage = mock(Message.class);
    Marker mockMarker = mock(Marker.class);
    GoogleMap mockGoogleMap = mock(GoogleMap.class);
    when(mockGoogleMap.addMarker(any())).thenReturn(mockMarker);

    MapsFragment mapsFragment = new MapsFragment(null, mockGoogleMap, null, false, 0);

    ArrayList<Message> mockMessages = new ArrayList<>();
    mockMessages.add(mockMessage);
    mapsFragment.onMessagesChanged(mockMessages);

    verify(mockGoogleMap).clear();
    verify(mockMarker).setTag(mockMessage);
  }

//  @Test
//  public void testPrepareBundleFromMarker() {
//    final double
//    final LatLng latLng = new LatLng(4.5, 6.7);
//    final Message message =
//        new Message("id", "userId", "https://www.example.com", "text", 12.3d, 23.4d, "timestamp");
//    Marker mockMarker = mock(Marker.class);
//    when(mockMarker.getPosition()).thenReturn(latLng);
//    when(mockMarker.getTag()).thenReturn(mockMessage);
//    Resources mockResources = mock(Resources.class);
//    when(mockResources.getString(R.string.args_is_user_data)).thenReturn("1");
//    when(mockResources.getString(R.string.args_latitude)).thenReturn("2");
//    when(mockResources.getString(R.string.args_longitude)).thenReturn("3");
//    when(mockResources.getString(R.string.args_text)).thenReturn("4");
//    when(mockResources.getString(R.string.args_image_uri)).thenReturn("5");
//    Bundle mockBundle = mock(Bundle.class);
//
//    Bundle res = fragment.prepareBundleFromMarker(mockBundle, mockMarker, mockResources);
//
//    verify(res).putBoolean("1", true);
//    verify(res).putDouble("2", 4.5);
//    verify(res).putDouble("3", 6.7);
//    verify(res).putString("4", "spongebob");
//    verify(res).putString("5", "squarepants.com");
//  }

}
