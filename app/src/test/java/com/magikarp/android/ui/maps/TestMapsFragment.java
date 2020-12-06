package com.magikarp.android.ui.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.Config.OLDEST_SDK;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.Gson;
import com.magikarp.android.R;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.ui.app.MainActivity;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowApplication;

/**
 * Class for testing {@code MapsFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
@LooperMode(PAUSED)
public class TestMapsFragment {

  @Mock
  ActivityResultLauncher<String> requestPermissionLauncher;
  @Mock
  Bundle arguments;
  @Mock
  FragmentActivity activity;
  @Mock
  GoogleMap googleMap;
  @Mock
  GoogleSignInAccount googleSignInAccount;
  @Mock
  MapsViewModel mapsViewModel;
  @Mock
  SharedPreferences preferences;

  private AutoCloseable closeable;

  private Context context;

  private MapsFragment fragment;

  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    context = ApplicationProvider.getApplicationContext();
    context.setTheme(R.style.Theme_Magikarp);
    fragment =
        new MapsFragment(requestPermissionLauncher, arguments, context, activity, googleMap,
            googleSignInAccount, mapsViewModel, preferences);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testDefaultConstructor() {
    new MapsFragment();

    // Confirm method completes.
  }

  @Test
  public void testPerformOnCreate() {
    when(arguments.getBoolean(anyString())).thenReturn(true);
    MapsFragment spy = spy(fragment);
    doReturn(activity).when(spy).requireActivity();
    doReturn(arguments).when(spy).requireArguments();
    doReturn(context).when(spy).requireContext();
    when(preferences.getString(anyString(), anyString())).thenReturn("100");

    spy.performOnCreate();

    assertTrue(spy.hasOptionsMenu());
    assertNotSame(requestPermissionLauncher, spy.requestPermissionLauncher);
    assertEquals(100, spy.maxRecords);
    verify(preferences).registerOnSharedPreferenceChangeListener(spy);
  }

  @Test
  public void testPerformOnCreateWithoutOptionsMenuOrPreferences() {
    final FragmentActivity activity = new MainActivity();
    when(arguments.getBoolean(anyString())).thenReturn(false);
    MapsFragment spy = spy(fragment);
    doReturn(activity).when(spy).requireActivity();
    doReturn(arguments).when(spy).requireArguments();
    doReturn(context).when(spy).requireContext();
    when(preferences.getString(anyString(), anyString())).thenReturn("100");

    spy.performOnCreate();

    assertFalse(spy.hasOptionsMenu());
    verify(preferences).registerOnSharedPreferenceChangeListener(spy);
  }

  @Test
  public void testOnCreateView() {
    final LayoutInflater inflater = mock(LayoutInflater.class);
    final ViewGroup container = mock(ViewGroup.class);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onCreateView(inflater, container, savedInstanceState);

    verify(inflater).inflate(R.layout.fragment_maps, container, false);
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testOnViewCreated() {
    final View view = mock(View.class);
    final Bundle savedInstanceState = mock(Bundle.class);
    final SupportMapFragment mapFragment = mock(SupportMapFragment.class);
    final FragmentManager fragmentManager = mock(FragmentManager.class);
    when(fragmentManager.findFragmentByTag(anyString())).thenReturn(mapFragment);
    MapsFragment spy = spy(fragment);
    doReturn(fragmentManager).when(spy).getChildFragmentManager();

    spy.onViewCreated(view, savedInstanceState);

    verify(mapFragment).getMapAsync(spy);
  }

  @Test
  public void testOnCreateOptionsMenu() {
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(anyInt(), eq(menu));
  }

  @Test
  public void testOnOptionsItemSelectedNewPost() {
    try (MockedStatic<NavHostFragment> navHostFragment = mockStatic(NavHostFragment.class)) {
      final MenuItem item = mock(MenuItem.class);
      when(item.getItemId()).thenReturn(R.id.action_new_post);
      final NavController navController = mock(NavController.class);
      // stub the static method that is called by the class under test
      navHostFragment.when(() -> NavHostFragment.findNavController(fragment))
          .thenReturn(navController);

      assertTrue(fragment.onOptionsItemSelected(item));

      verify(navController).navigate(eq(R.id.action_nav_maps_to_post_editor), any(Bundle.class));
    }
  }

  @Test
  public void testOnOptionsItemSelectedUnspecified() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(Integer.MAX_VALUE);

    assertFalse(fragment.onOptionsItemSelected(item));
  }

  @Test
  public void testOnDestroy() {
    fragment.onDestroy();

    assertNull(fragment.activity);
    assertNull(fragment.arguments);
    assertNull(fragment.context);
    verify(preferences).unregisterOnSharedPreferenceChangeListener(fragment);
  }

  @Test
  public void testOnMapReadyNoPermission() {
    final ShadowApplication application = Shadows.shadowOf((Application) context);
    application.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
    final MapsFragment spy = spy(fragment);
    doNothing().when(spy).onCameraIdle();
    when(mapsViewModel.getMessages()).thenReturn(new MutableLiveData<>());

    spy.onMapReady(googleMap);

    verify(requestPermissionLauncher).launch(Manifest.permission.ACCESS_FINE_LOCATION);
    verify(googleMap).setOnCameraIdleListener(spy);
    verify(googleMap).setOnMarkerClickListener(spy);
    verify(mapsViewModel).getMessages();
    verifyNoMoreInteractions(googleMap);
  }

  @Test
  public void testOnMapReadyFineLocationPermission() {
    final ShadowApplication application = Shadows.shadowOf((Application) context);
    application.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
    final MapsFragment spy = spy(fragment);
    doNothing().when(spy).onCameraIdle();
    when(mapsViewModel.getMessages()).thenReturn(new MutableLiveData<>());

    spy.onMapReady(googleMap);

    verify(googleMap).setMyLocationEnabled(true);
    verify(googleMap).setOnCameraIdleListener(spy);
    verify(googleMap).setOnMarkerClickListener(spy);
    verify(mapsViewModel).getMessages();
    verifyNoInteractions(requestPermissionLauncher);
  }

  @Test
  public void testOnRequestPermissionResultTrueAndMapNotNull() {
    fragment.onRequestPermissionResult(true);

    verify(googleMap).setMyLocationEnabled(true);
    verifyNoMoreInteractions(googleMap);
  }

  @Test
  public void testOnRequestPermissionResultTrueButMapNull() {
    fragment.googleMap = null;

    fragment.onRequestPermissionResult(true);

    // Confirm method completes.
  }

  @Test
  public void testOnRequestPermissionResultFalse() {
    fragment.onRequestPermissionResult(false);

    verifyNoInteractions(googleMap);
  }

  @Test
  public void testOnRequestPermissionResultThrowsSecurityException() {
    doThrow(SecurityException.class).when(googleMap).setMyLocationEnabled(true);

    fragment.onRequestPermissionResult(true);

    verify(googleMap).setMyLocationEnabled(false);
  }

  @Test
  public void testOnCameraIdleIsUserData() {
    final String userId = "userId";
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
    when(googleSignInAccount.getId()).thenReturn(userId);
    final LatLngBounds latLngBounds =
        new LatLngBounds(new LatLng(1.0d, 2.0d), new LatLng(3.0d, 4.0d));
    final VisibleRegion visibleRegion =
        new VisibleRegion(new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0),
            latLngBounds);
    final Projection projection = mock(Projection.class);
    when(projection.getVisibleRegion()).thenReturn(visibleRegion);
    when(googleMap.getProjection()).thenReturn(projection);
    fragment.maxRecords = 100;

    fragment.onCameraIdle();

    verify(mapsViewModel)
        .setMapsQuery(eq(userId), eq(latLngBounds), eq(100), any(ErrorListener.class));
  }

  @Test
  public void testOnCameraIdleIsNotUserData() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(false);
    when(googleSignInAccount.getId()).thenReturn("userId");
    final LatLngBounds latLngBounds =
        new LatLngBounds(new LatLng(1.0d, 2.0d), new LatLng(3.0d, 4.0d));
    final VisibleRegion visibleRegion =
        new VisibleRegion(new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0),
            latLngBounds);
    final Projection projection = mock(Projection.class);
    when(projection.getVisibleRegion()).thenReturn(visibleRegion);
    when(googleMap.getProjection()).thenReturn(projection);
    fragment.maxRecords = 100;

    fragment.onCameraIdle();

    verify(mapsViewModel)
        .setMapsQuery(eq(null), eq(latLngBounds), eq(100), any(ErrorListener.class));
  }

  @Test
  public void testOnMapsQueryError() {
    final VolleyError error = mock(VolleyError.class);
    fragment.wasQueryError = false;
    when(activity.findViewById(android.R.id.content)).thenReturn(new CoordinatorLayout(context));

    fragment.onMapsQueryError(error);

    verify(activity).findViewById(android.R.id.content);
    assertTrue(fragment.wasQueryError);
  }

  @Test
  public void testOnMapsQueryErrorWasQueryError() {
    final VolleyError error = mock(VolleyError.class);
    fragment.wasQueryError = true;
    when(activity.findViewById(android.R.id.content)).thenReturn(new CoordinatorLayout(context));

    fragment.onMapsQueryError(error);

    verify(activity, never()).findViewById(anyInt());
  }

  @Test
  public void testOnMapsQueryErrorActivityNull() {
    final VolleyError error = mock(VolleyError.class);
    fragment.wasQueryError = false;
    fragment.activity = null;

    fragment.onMapsQueryError(error);

    assertFalse(fragment.wasQueryError);
  }

  @Test
  public void testOnGoogleSignInAccountChangedAccountNull() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
    when(googleSignInAccount.getId()).thenReturn("userId");
    final GoogleSignInAccount account = mock(GoogleSignInAccount.class);
    when(account.getId()).thenReturn("notUserId");

    fragment.onGoogleSignInAccountChanged(null);

    verify(activity).onBackPressed();
    assertSame(googleSignInAccount, fragment.googleSignInAccount);
  }

  @Test
  public void testOnGoogleSignInAccountChangedDifferentAccount() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
    when(googleSignInAccount.getId()).thenReturn("userId");
    final GoogleSignInAccount account = mock(GoogleSignInAccount.class);
    when(account.getId()).thenReturn("notUserId");

    fragment.onGoogleSignInAccountChanged(account);

    verify(activity).onBackPressed();
    assertSame(googleSignInAccount, fragment.googleSignInAccount);
  }

  @Test
  public void testOnGoogleSignInAccountChangedIsNotUserData() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(false);
    when(googleSignInAccount.getId()).thenReturn("userId");

    fragment.onGoogleSignInAccountChanged(null);

    verifyNoInteractions(activity);
  }

  @Test
  public void testOnGoogleSignInAccountChangedGoogleSignInAccountNull() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
    fragment.googleSignInAccount = null;

    fragment.onGoogleSignInAccountChanged(null);

    verifyNoInteractions(activity);
  }

  @Test
  public void testOnGoogleSignInAccountChangedGoogleSignInSameAccount() {
    when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
    when(googleSignInAccount.getId()).thenReturn("userId");

    fragment.onGoogleSignInAccountChanged(fragment.googleSignInAccount);

    verifyNoInteractions(activity);
    assertNotNull(fragment.googleSignInAccount);
  }

  @Test
  public void testOnMessagesChanged() {
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    List<Message> messages = new ArrayList<>(3);
    messages.add(message);
    messages.add(message);
    messages.add(message);
    final Marker marker = mock(Marker.class);
    when(googleMap.addMarker(any(MarkerOptions.class))).thenReturn(marker);
    fragment.wasQueryError = true;

    fragment.onMessagesChanged(messages);

    verify(googleMap).clear();
    verify(googleMap, times(3)).addMarker(any(MarkerOptions.class));
    verify(marker, times(3)).setTag(message);
    assertFalse(fragment.wasQueryError);
  }

  @Test
  public void testOnMessagesChangedGoogleMapNull() {
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    final List<Message> messages = new ArrayList<>(3);
    messages.add(message);
    messages.add(message);
    messages.add(message);
    final Marker marker = mock(Marker.class);
    when(googleMap.addMarker(any(MarkerOptions.class))).thenReturn(marker);
    fragment.googleMap = null;

    fragment.onMessagesChanged(messages);

    verifyNoInteractions(googleMap);
  }

  @Test
  public void testOnMessagesChangedMessagesNull() {
    final Marker marker = mock(Marker.class);
    when(googleMap.addMarker(any(MarkerOptions.class))).thenReturn(marker);

    fragment.onMessagesChanged(null);

    verifyNoInteractions(googleMap);
  }

  @Test
  public void testOnMessagesChangedMessagesEmpty() {
    List<Message> messages = new ArrayList<>(0);
    final Marker marker = mock(Marker.class);
    when(googleMap.addMarker(any(MarkerOptions.class))).thenReturn(marker);

    fragment.onMessagesChanged(messages);

    verifyNoInteractions(googleMap);
  }

  @Test
  public void testOnMarkerClickIsUserData() {
    try (MockedStatic<NavHostFragment> navHostFragment = mockStatic(NavHostFragment.class)) {
      final NavController navController = mock(NavController.class);
      navHostFragment.when(() -> NavHostFragment.findNavController(fragment))
          .thenReturn(navController);

      when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(true);
      final Marker marker = mock(Marker.class);
      final Message messageIn =
          new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
      when(marker.getTag()).thenReturn(messageIn);
      final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

      fragment.onMarkerClick(marker);

      verify(navController).navigate(eq(R.id.action_nav_maps_to_post_editor), captor.capture());
      final Message messageOut =
          captor.getValue().getParcelable(context.getString(R.string.args_message));
      assertEquals(new Gson().toJson(messageIn), new Gson().toJson(messageOut));
    }
  }

  @Test
  public void testOnMarkerClickIsNotUserData() {
    try (MockedStatic<NavHostFragment> navHostFragment = mockStatic(NavHostFragment.class)) {
      final NavController navController = mock(NavController.class);
      navHostFragment.when(() -> NavHostFragment.findNavController(fragment))
          .thenReturn(navController);

      when(arguments.getBoolean(context.getString(R.string.args_is_user_data))).thenReturn(false);
      final Marker marker = mock(Marker.class);
      final Message messageIn =
          new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
      when(marker.getTag()).thenReturn(messageIn);
      final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

      fragment.onMarkerClick(marker);

      verify(navController).navigate(eq(R.id.action_nav_maps_to_post_viewer), captor.capture());
      final Message messageOut =
          captor.getValue().getParcelable(context.getString(R.string.args_message));
      assertEquals(new Gson().toJson(messageIn), new Gson().toJson(messageOut));
    }
  }

  @Test
  public void testOnSharedPreferenceChangedMaxRecords() {
    final SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    when(sharedPreferences.getString(anyString(), anyString())).thenReturn("100");
    final String key = context.getString(R.string.preference_key_max_records);

    fragment.maxRecords = 20;
    fragment.onSharedPreferenceChanged(sharedPreferences, key);

    assertEquals(100, fragment.maxRecords);
  }

  @Test
  public void testOnSharedPreferenceChangedNotMaxRecords() {
    final SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    when(sharedPreferences.getString(anyString(), anyString())).thenReturn("100");
    final String key = "notMaxRecords";

    fragment.maxRecords = 5;
    fragment.onSharedPreferenceChanged(sharedPreferences, key);

    assertEquals(5, fragment.maxRecords);
  }

}
