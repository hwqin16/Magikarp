package com.magikarp.android.ui.maps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.magikarp.android.R;
import com.magikarp.android.data.model.Message;
import java.util.ArrayList;
import org.junit.Test;

public class TestMapsFragment {
  @Test
  public void testPerformOnCreate() {
    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);
    ViewModelProvider mockViewModelProvider = mock(ViewModelProvider.class);
    when(mockViewModelProvider.get(MapsViewModel.class)).thenReturn(mockMapsViewModel);

    boolean hasOptionsMenu = true;
    String argsIsUserData = "arrrg";
    Bundle mockArguments = mock(Bundle.class);
    when(mockArguments.getBoolean(argsIsUserData)).thenReturn(hasOptionsMenu);
    MapsFragment mapsFragment = new MapsFragment();
    mapsFragment.setArguments(mockArguments);

    mapsFragment.performOnCreate(mockViewModelProvider, argsIsUserData, null);

    assertTrue(mapsFragment.hasOptionsMenu());
  }

  @Test
  public void testPerformOnCreateOptionsMenu() {
    Menu mockMenu = mock(Menu.class);
    MenuInflater mockMenuInflater = mock(MenuInflater.class);

    MapsFragment mapsFragment = new MapsFragment();

    mapsFragment.performOnCreateOptionsMenu(mockMenu, mockMenuInflater);

    verify(mockMenuInflater).inflate(R.menu.menu_maps, mockMenu);
  }

  @Test
  public void testOnCreateView() {
    ViewGroup mockViewGroup = mock(ViewGroup.class);
    LayoutInflater mockLayoutInflater = mock(LayoutInflater.class);

    MapsFragment mapsFragment = new MapsFragment();

    mapsFragment.onCreateView(mockLayoutInflater, mockViewGroup, null);

    verify(mockLayoutInflater).inflate(R.layout.fragment_maps, mockViewGroup, false);
  }

  @Test
  public void testOnOptionsItemSelected() {
    int id = R.id.nav_post_editor - 1;
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(id);

    MapsFragment mapsFragment = new MapsFragment();

    assertFalse(mapsFragment.onOptionsItemSelected(mockMenuItem));
  }

  @Test
  public void testOnMapReady() {
    GoogleMap mockGoogleMap = mock(GoogleMap.class);
    LiveData mockLiveData = mock(LiveData.class);
    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);
    when(mockMapsViewModel.getMessages()).thenReturn(mockLiveData);

    MapsFragment mapsFragment = new MapsFragment(mockMapsViewModel, null);

    mapsFragment.onMapReady(mockGoogleMap);

    verify(mockGoogleMap).setOnCameraIdleListener(mapsFragment);
    verify(mockGoogleMap).setOnMarkerClickListener(mapsFragment);
    verify(mockLiveData).observe(mapsFragment, mapsFragment);
  }

  @Test
  public void testOnCameraIdle() {
    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(1.0, 1.0));
    VisibleRegion visibleRegion = new VisibleRegion(null, null, null, null, latLngBounds);
    Projection mockProjection = mock(Projection.class);
    when(mockProjection.getVisibleRegion()).thenReturn(visibleRegion);
    GoogleMap mockGoogleMap = mock(GoogleMap.class);
    when(mockGoogleMap.getProjection()).thenReturn(mockProjection);
    MapsViewModel mockMapsViewModel = mock(MapsViewModel.class);

    MapsFragment mapsFragment = new MapsFragment(mockMapsViewModel, mockGoogleMap);

    mapsFragment.onCameraIdle();

    verify(mockMapsViewModel).setMapsQuery(null, latLngBounds, 20);
  }

  @Test
  public void testOnChanged() {
    Message mockMessage = mock(Message.class);
    Marker mockMarker = mock(Marker.class);
    GoogleMap mockGoogleMap = mock(GoogleMap.class);
    when(mockGoogleMap.addMarker(any())).thenReturn(mockMarker);

    MapsFragment mapsFragment = new MapsFragment(null, mockGoogleMap);

    ArrayList<Message> mockMessages = new ArrayList<>();
    mockMessages.add(mockMessage);
    mapsFragment.onChanged(mockMessages);

    verify(mockGoogleMap).clear();
    verify(mockMarker).setTag(mockMessage);
  }

  @Test
  public void testPrepareBundleFromMarker() {
    LatLng latLng = new LatLng(4.5, 6.7);
    Message mockMessage = mock(Message.class);
    when(mockMessage.getText()).thenReturn("spongebob");
    when(mockMessage.getImageUrl()).thenReturn("squarepants.com");
    Marker mockMarker = mock(Marker.class);
    when(mockMarker.getPosition()).thenReturn(latLng);
    when(mockMarker.getTag()).thenReturn(mockMessage);
    Resources mockResources = mock(Resources.class);
    when(mockResources.getString(R.string.args_is_user_data)).thenReturn("1");
    when(mockResources.getString(R.string.args_latitude)).thenReturn("2");
    when(mockResources.getString(R.string.args_longitude)).thenReturn("3");
    when(mockResources.getString(R.string.args_text)).thenReturn("4");
    when(mockResources.getString(R.string.args_image_uri)).thenReturn("5");
    Bundle mockBundle = mock(Bundle.class);

    MapsFragment mapsFragment = new MapsFragment(true);

    Bundle res = mapsFragment.prepareBundleFromMarker(mockBundle, mockMarker, mockResources);
    verify(res).putBoolean("1", true);
    verify(res).putDouble("2", 4.5);
    verify(res).putDouble("3", 6.7);
    verify(res).putString("4", "spongebob");
    verify(res).putString("5", "squarepants.com");
  }
}
