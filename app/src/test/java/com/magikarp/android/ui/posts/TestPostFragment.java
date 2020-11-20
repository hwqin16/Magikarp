package com.magikarp.android.ui.posts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.magikarp.android.R;
import com.magikarp.android.services.LocationService;
import org.junit.Test;

public class TestPostFragment {
  @Test
  public void testPerformOnCreate() {
    Context mockContext = mock(Context.class);
    Intent mockGPSIntent = mock(Intent.class);
    ServiceConnection mockServiceConnection = mock(ServiceConnection.class);

    PostFragment postFragment = new PostFragment(mockServiceConnection, null, true);

    assertFalse(postFragment.hasOptionsMenu());

    postFragment.performOnCreate(mockContext, mockGPSIntent);

    assertTrue(postFragment.hasOptionsMenu());
    verify(mockContext).bindService(mockGPSIntent, mockServiceConnection, Context.BIND_AUTO_CREATE);
    verify(mockContext).startService(mockGPSIntent);
  }

  @Test
  public void testPerformOnCreateOptionsMenu() {
    Menu mockMenu = mock(Menu.class);
    MenuInflater mockInflater = mock(MenuInflater.class);

    PostFragment postFragment = new PostFragment();

    postFragment.performOnCreateOptionsMenu(mockMenu, mockInflater, false);

    verify(mockInflater).inflate(R.menu.menu_post_view, mockMenu);
  }

  @Test
  public void testOnCreateView() {
    ViewGroup mockContainer = mock(ViewGroup.class);
    LayoutInflater mockInflater = mock(LayoutInflater.class);

    PostFragment postFragment = new PostFragment();

    postFragment.onCreateView(mockInflater, mockContainer, null);

    verify(mockInflater).inflate(R.layout.fragment_post, mockContainer, false);
  }

  @Test
  public void testPerformOnViewCreated() {
    Double latitude = 12.4;
    Double longitude = 4.6;
    String text = "hello moto";
    String isEditable = "editable";

    Bundle mockSavedInstanceState = mock(Bundle.class);
    when(mockSavedInstanceState.getDouble(PostFragment.SAVESTATE_LATITUDE)).thenReturn(latitude);
    when(mockSavedInstanceState.getDouble(PostFragment.SAVESTATE_LONGITUDE)).thenReturn(longitude);
    when(mockSavedInstanceState.getParcelable(PostFragment.SAVESTATE_IMAGE_URI)).thenReturn(null);
    when(mockSavedInstanceState.getString(PostFragment.SAVESTATE_TEXT)).thenReturn(text);

    Bundle mockArgs = mock(Bundle.class);
    when(mockArgs.getBoolean(isEditable)).thenReturn(false);

    NetworkImageView mockNetworkImageView = mock(NetworkImageView.class);
    EditText mockEditText = mock(EditText.class);
    View mockView = mock(View.class);
    when(mockView.findViewById(R.id.create_post_network_image)).thenReturn(mockNetworkImageView);
    when(mockView.findViewById(R.id.create_post_caption)).thenReturn(mockEditText);

    PostFragment postFragment = new PostFragment();

    postFragment.performOnViewCreated(mockView, mockSavedInstanceState, mockArgs, isEditable);

    verify(mockNetworkImageView).setDefaultImageResId(R.drawable.ic_menu_gallery);
    verify(mockNetworkImageView).setErrorImageResId(R.drawable.ic_menu_gallery);
    verify(mockEditText).setText(text);
    verify(mockEditText).setEnabled(false);
    verify(mockEditText).setFocusable(false);
    verify(mockEditText).setFocusableInTouchMode(false);
  }

  @Test
  public void testPerformOnSaveInstanceState() {
    Bundle bundle = mock(Bundle.class);

    PostFragment postFragment = new PostFragment();

    postFragment.performOnSaveInstanceState(bundle, false);

    verifyNoInteractions(bundle);
  }

  @Test
  public void testPerformOnDestroy() {
    Context context = mock(Context.class);
    ServiceConnection mockGPSServiceConnection = mock(ServiceConnection.class);
    LocationService mockGPSService = mock(LocationService.class);

    PostFragment postFragment = new PostFragment(mockGPSServiceConnection, mockGPSService, true);

    postFragment.performOnDestroy(context);

    verify(context).unbindService(mockGPSServiceConnection);
    verify(mockGPSService).dispose();
  }

  @Test
  public void testPerformOnActivityResult() {
    Intent mockData = mock(Intent.class);

    PostFragment postFragment = new PostFragment();

    postFragment.performOnActivityResult(
        PostFragment.RESULT_LOAD_IMG - 1,
        Activity.RESULT_CANCELED,
        mockData
    );

    verifyNoInteractions(mockData);
  }

  @Test
  public void testPerformLoadImage() {
    String uriString = "http://example.com/image.png";

    NetworkImageView mockNetworkImageView = mock(NetworkImageView.class);
    ImageView mockImageView = mock(ImageView.class);
    View mockView = mock(View.class);
    when(mockView.findViewById(R.id.create_post_network_image)).thenReturn(mockNetworkImageView);
    when(mockView.findViewById(R.id.create_post_local_image)).thenReturn(mockImageView);

    Uri mockImageUri = mock(Uri.class);
    when(mockImageUri.toString()).thenReturn(uriString);
    ImageLoader mockImageLoader = mock(ImageLoader.class);
    when(mockImageLoader.get(any(), any())).thenReturn(null);
    Context mockContext = mock(Context.class);

    PostFragment postFragment = new PostFragment(mockImageLoader);

    postFragment.performLoadImage(mockView, mockContext, mockImageUri, "http");

    verify(mockNetworkImageView).setVisibility(View.VISIBLE);
    verify(mockImageView).setVisibility(View.INVISIBLE);
    verify(mockNetworkImageView).setImageUrl(uriString, mockImageLoader);
  }

  @Test
  public void testOnGpsButtonClick() {
    MenuItem mockMenuItem = mock(MenuItem.class);

    PostFragment postFragment = new PostFragment(null, null, false);

    postFragment.onGpsButtonClick(mockMenuItem);

    verifyNoInteractions(mockMenuItem);
  }

  @Test
  public void testPerformOnRequestPermissionsResult() {
    Context context = mock(Context.class);

    PostFragment postFragment = new PostFragment();

    postFragment.performOnRequestPermissionsResult(context, 0, new int[] {});

    verifyNoInteractions(context);
  }

  @Test
  public void testPerformOnPostButtonClick() {
    EditText mockEditText = mock(EditText.class);
    View mockView = mock(View.class);
    when(mockView.findViewById(R.id.create_post_caption)).thenReturn(mockEditText);
    Toast mockOneOrMoreFields = mock(Toast.class);

    PostFragment postFragment = new PostFragment(2.2, 3.3, null);

    postFragment.performOnPostButtonClick(null, null, mockView, null, mockOneOrMoreFields, null);

    verify(mockOneOrMoreFields).show();
  }
}
