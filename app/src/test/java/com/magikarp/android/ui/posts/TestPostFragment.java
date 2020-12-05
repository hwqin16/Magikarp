package com.magikarp.android.ui.posts;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Verify.verifyNotNull;
import static com.magikarp.android.ui.posts.PostFragment.SAVE_STATE_IMAGE_URL;
import static com.magikarp.android.ui.posts.PostFragment.SAVE_STATE_LOCATION;
import static com.magikarp.android.ui.posts.PostFragment.SAVE_STATE_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.Config.OLDEST_SDK;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.magikarp.android.R;
import com.magikarp.android.data.PostRepository;
import com.magikarp.android.data.PostRepository.UploadUriListener;
import com.magikarp.android.data.model.DeleteMessageResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.data.model.UpdateMessageResponse;
import com.magikarp.android.databinding.FragmentPostBinding;
import com.magikarp.android.location.LocationListener;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowApplication;

/**
 * Class for testing {@code PostFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
@LooperMode(PAUSED)
public class TestPostFragment {

  private final String imageUrl = "imageUrl";
  @Mock
  ActivityResultLauncher<String> getContentLauncher;
  @Mock
  ActivityResultLauncher<String> requestPermissionLauncher;
  @Mock
  private Bundle arguments;
  @Mock
  FragmentActivity activity;
  @Mock
  GoogleSignInAccount googleSignInAccount;
  @Mock
  private LatLng location;
  @Mock
  LocationListener locationListener;
  @Mock
  ContentResolver contentResolver;
  @Mock
  FusedLocationProviderClient fusedLocationClient;
  @Mock
  private ImageLoader imageLoader;
  @Mock
  private PostRepository postRepository;
  @Mock
  private RequestQueue requestQueue;

  private AutoCloseable closeable;

  private Context context;

  private FragmentPostBinding binding;

  private PostFragment fragment;

  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    context = ApplicationProvider.getApplicationContext();
    binding = FragmentPostBinding.inflate(LayoutInflater.from(context));
    fragment =
        new PostFragment(
            getContentLauncher,
            requestPermissionLauncher,
            arguments,
            context,
            activity,
            binding,
            googleSignInAccount,
            location,
            locationListener,
            imageUrl,
            contentResolver,
            fusedLocationClient,
            imageLoader,
            postRepository,
            requestQueue);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testDefaultConstructor() {
    new PostFragment();

    // Confirm method completes.
  }

  @Test
  public void testPerformOnCreate() {
    final PostFragment spy = spy(fragment);
    doReturn(activity).when(spy).requireActivity();
    doReturn(arguments).when(spy).requireArguments();
    doReturn(context).when(spy).requireContext();

    assertFalse(spy.hasOptionsMenu());

    spy.performOnCreate();
    shadowOf(getMainLooper()).idle();

    assertTrue(spy.hasOptionsMenu());
  }

  @Test
  public void testOnCreateOptionsMenuPostTypeNew() {
    final String argsPostType = context.getString(R.string.args_post_type);
    final String argPostTypeNew = context.getString(R.string.arg_post_type_new);
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);
    when(arguments.getString(argsPostType)).thenReturn(argPostTypeNew);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(R.menu.menu_post_new, menu);
  }

  @Test
  public void testOnCreateOptionsMenuPostTypeUpdate() {
    final String argsPostType = context.getString(R.string.args_post_type);
    final String argPostTypeUpdate = context.getString(R.string.arg_post_type_update);
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);
    when(arguments.getString(argsPostType)).thenReturn(argPostTypeUpdate);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(R.menu.menu_post_update, menu);
  }


  @Test
  public void testOnCreateOptionsMenuPostTypeView() {
    final String argsPostType = context.getString(R.string.args_post_type);
    final String argPostTypeView = context.getString(R.string.arg_post_type_view);
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);
    when(arguments.getString(argsPostType)).thenReturn(argPostTypeView);

    fragment.onCreateOptionsMenu(menu, inflater);

    verify(inflater).inflate(R.menu.menu_post_view, menu);
  }

  @Test
  public void testOnCreateOptionsMenuIllegalPostType() {
    final String argsPostType = context.getString(R.string.args_post_type);
    final String argPostTypeIllegal = "illegalArgument";
    final Menu menu = mock(Menu.class);
    final MenuInflater inflater = mock(MenuInflater.class);
    when(arguments.getString(argsPostType)).thenReturn(argPostTypeIllegal);

    assertThrows(IllegalArgumentException.class,
        () -> fragment.onCreateOptionsMenu(menu, inflater));
  }

  @Test
  public void testOnOptionsItemSelectedGetLocation() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_get_location);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onLocationButtonClick();

    assertTrue(spy.onOptionsItemSelected(item));
  }

  @Test
  public void testOnOptionsItemSelectedPost() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_upload_content);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onPostButtonClick();

    assertTrue(spy.onOptionsItemSelected(item));
  }

  @Test
  public void testOnOptionsItemSelectedDelete() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_delete);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onDeleteButtonClick();

    assertTrue(spy.onOptionsItemSelected(item));
  }

  @Test
  public void testOnOptionsItemSelectedGetDirections() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(R.id.menu_get_directions);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).startActivityFromIntent(any(Intent.class));

    assertTrue(spy.onOptionsItemSelected(item));

    verify(spy).startActivityFromIntent(any(Intent.class));
  }

  @Test
  public void testOnOptionsItemSelectedUnspecified() {
    final MenuItem item = mock(MenuItem.class);
    when(item.getItemId()).thenReturn(Integer.MAX_VALUE);

    assertFalse(fragment.onOptionsItemSelected(item));
  }

  @Test
  public void testOnCreateView() {
    final LayoutInflater inflater = LayoutInflater.from(context);
    final Bundle savedInstanceState = mock(Bundle.class);

    fragment.onCreateView(inflater, null, savedInstanceState);

    assert (fragment.binding != binding);
  }

  @Test
  public void testOnViewCreatedPostTypeNew() {
    final View view = mock(View.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_new));
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(anyString());

    spy.onViewCreated(view, null);

    assertSame(location, spy.location);
    assertSame(imageUrl, spy.imageUrl);
    assertTrue(TextUtils.isEmpty(spy.binding.createPostCaption.getText().toString()));
  }

  @Test
  public void testOnViewCreatedNoSavedInstanceStatePostTypeUpdate() {
    final View view = mock(View.class);
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_update));
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(anyString());

    spy.onViewCreated(view, null);

    assertEquals(new LatLng(message.getLatitude(), message.getLongitude()), spy.location);
    assertSame("imageUrl", spy.imageUrl);
    assertEquals("text", spy.binding.createPostCaption.getText().toString());
  }

  @Test
  public void testOnViewCreatedNoSavedInstanceStatePostTypeView() {
    final View view = mock(View.class);
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_view));
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(anyString());

    spy.onViewCreated(view, null);

    assertEquals(new LatLng(message.getLatitude(), message.getLongitude()), spy.location);
    assertSame("imageUrl", spy.imageUrl);
    assertEquals("text", spy.binding.createPostCaption.getText().toString());
  }

  @Test
  public void testOnViewCreatedWithSavedInstanceState() {
    final View view = mock(View.class);
    final Bundle savedInstanceState = mock(Bundle.class);
    final LatLng latLng = mock(LatLng.class);
    when(arguments.getString(context.getString(R.string.args_post_type))).thenReturn("any");
    when(savedInstanceState.getParcelable(SAVE_STATE_LOCATION)).thenReturn(latLng);
    when(savedInstanceState.getString(SAVE_STATE_IMAGE_URL)).thenReturn("imageUrl");
    when(savedInstanceState.getString(SAVE_STATE_TEXT)).thenReturn("text");
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(anyString());

    spy.onViewCreated(view, savedInstanceState);

    assertSame(latLng, spy.location);
    assertSame("imageUrl", spy.imageUrl);
    assertEquals("text", spy.binding.createPostCaption.getText().toString());
  }

  @Test
  public void testOnStartNoPermission() {
    final ShadowApplication application = Shadows.shadowOf((Application) context);
    application.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION);

    fragment.onStart();

    assertSame(locationListener, fragment.locationListener);

    verify(requestPermissionLauncher).launch(Manifest.permission.ACCESS_FINE_LOCATION);
    verifyNoInteractions(fusedLocationClient);
  }

  @Test
  public void testOnStartWithFineLocationPermission() {
    final ShadowApplication application = Shadows.shadowOf((Application) context);
    application.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);

    fragment.onStart();

    assert (locationListener != fragment.locationListener);
    verifyNotNull(fragment.locationListener);
    verify(fusedLocationClient)
        .requestLocationUpdates(any(LocationRequest.class), any(LocationCallback.class),
            nullable(Looper.class));
    verifyNoInteractions(requestPermissionLauncher);
  }

  @Test
  public void testOnPauseLocationListenerNotNull() {
    fragment.onPause();

    verify(fusedLocationClient).removeLocationUpdates(any(LocationCallback.class));
    assertNull(fragment.locationListener);
  }

  @Test
  public void testOnPauseLocationListenerNull() {
    fragment.locationListener = null;

    fragment.onPause();

    verifyNoInteractions(fusedLocationClient);
  }

  @Test
  public void testOnSaveInstanceStatePostTypeNew() {
    final Bundle bundle = mock(Bundle.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_new));

    fragment.onSaveInstanceState(bundle);

    verify(bundle).putParcelable(anyString(), any());
  }

  @Test
  public void testOnSaveInstanceStatePostTypeUpdate() {
    final Bundle bundle = mock(Bundle.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_update));

    fragment.onSaveInstanceState(bundle);

    verify(bundle).putParcelable(anyString(), any());
  }

  @Test
  public void testOnSaveInstanceStatePostTypeView() {
    final Bundle bundle = mock(Bundle.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_view));

    fragment.onSaveInstanceState(bundle);

    verifyNoInteractions(bundle);
  }

  @Test
  public void testOnDestroyView() {
    fragment.onDestroyView();

    assertNull(fragment.binding);
  }

  @Test
  public void testOnDestroy() {
    fragment.onDestroy();
    shadowOf(getMainLooper()).idle();

    assertNull(fragment.activity);
    assertNull(fragment.context);
    assertNull(fragment.arguments);
  }

  @Test
  public void testStartActivityFromIntent() {
    final Intent intent = mock(Intent.class);
    final ComponentName componentName = mock(ComponentName.class);
    when(intent.resolveActivity(any())).thenReturn(componentName);

    fragment.startActivityFromIntent(intent);

    verify(activity).startActivity(intent);
  }

  @Test
  public void testStartActivityFromIntentNoActivityAvailable() {
    final Intent intent = mock(Intent.class);
    when(intent.resolveActivity(any())).thenReturn(null);

    fragment.startActivityFromIntent(intent);

    verify(activity, never()).startActivity(any());
  }

  @Test
  public void testOnRequestPermissionResultTrue() {
    fragment.onRequestPermissionResult(true);

    assertNotSame(locationListener, fragment.locationListener);
    verify(fusedLocationClient)
        .requestLocationUpdates(any(LocationRequest.class), any(LocationListener.class),
            nullable(Looper.class));
    verifyNoMoreInteractions(fusedLocationClient);
  }

  @Test
  public void testOnRequestPermissionResultFalse() {
    fragment.onRequestPermissionResult(false);

    assertSame(locationListener, fragment.locationListener);
    verifyNoInteractions(fusedLocationClient);
  }

  @Test
  public void testOnRequestPermissionResultSecurityException() {
    doThrow(SecurityException.class).when(fusedLocationClient)
        .requestLocationUpdates(any(LocationRequest.class), any(LocationCallback.class),
            nullable(Looper.class));

    fragment.onRequestPermissionResult(true);

    assertNull(fragment.locationListener);
    verify(fusedLocationClient).removeLocationUpdates(any(LocationCallback.class));
  }

  @Test
  public void testOnGoogleSignInAccountChanged() {
    final GoogleSignInAccount account = mock(GoogleSignInAccount.class);

    fragment.onGoogleSignInAccountChanged(account);

    assertEquals(account, fragment.googleSignInAccount);
  }

  @Test
  public void testOnGoogleSignInAccountChangedLogOut() {
    final GoogleSignInAccount account = fragment.googleSignInAccount;

    fragment.onGoogleSignInAccountChanged(account);

    assertEquals(account, fragment.googleSignInAccount);
  }

  @Test
  public void testOnGetContentResultNotNull() {
    final Uri uri = mock(Uri.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(nullable(String.class));

    spy.onGetContentResult(uri);

    verify(spy).loadImage(any(String.class));
  }

  @Test
  public void testOnGetContentResultNull() {
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(nullable(String.class));

    spy.onGetContentResult(null);

    verify(spy, never()).loadImage(nullable(String.class));
  }

  @Test
  public void testLoadImageHttpSchema() {
    final String imageUrl = "http://example.com/image.png";

    fragment.loadImage(imageUrl);

    assertEquals(View.VISIBLE, binding.createPostNetworkImage.getVisibility());
    assertEquals(View.INVISIBLE, binding.createPostLocalImage.getVisibility());
    verify(imageLoader).get(eq(imageUrl), any(ImageLoader.ImageListener.class));
  }

  @Test
  public void testLoadImageFileSchema() throws FileNotFoundException {
    try (MockedStatic<BitmapFactory> bitmapFactory = mockStatic(BitmapFactory.class)) {
      final String imageUrl = "file:///images/image.png";
      final InputStream inputStream = mock(InputStream.class);
      when(contentResolver.openInputStream(any(Uri.class))).thenReturn(inputStream);
      final Bitmap bitmap = Bitmap.createBitmap(new int[] {0}, 1, 1, Bitmap.Config.ARGB_8888);
      bitmapFactory.when(() -> BitmapFactory.decodeStream(inputStream)).thenReturn(bitmap);

      fragment.loadImage(imageUrl);

      assertEquals(View.INVISIBLE, binding.createPostNetworkImage.getVisibility());
      assertEquals(View.VISIBLE, binding.createPostLocalImage.getVisibility());
      verifyNoInteractions(imageLoader);
    }
  }

  @Test
  public void testLoadImageFileSchemaFileNotFound() throws FileNotFoundException {
    final String imageUrl = "file:///images/image.png";
    doThrow(FileNotFoundException.class).when(contentResolver).openInputStream(any(Uri.class));

    fragment.loadImage(imageUrl);

    assertNull(fragment.imageUrl);
  }

  @Test
  public void testOnLocationButtonClickLocationListenerNotNullWithLocation() {
    final Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0d);
    when(location.getLongitude()).thenReturn(2.0d);
    when(locationListener.getLocation()).thenReturn(location);

    fragment.onLocationButtonClick();

    assertEquals(new LatLng(1.0d, 2.0d), fragment.location);
  }

  @Test
  public void testOnLocationButtonClickLocationListenerNotNullButNoLocation() {
    when(locationListener.getLocation()).thenReturn(null);

    fragment.onLocationButtonClick();

    assertSame(location, fragment.location);
  }

  @Test
  public void testOnLocationButtonClickLocationListenerNull() {
    fragment.locationListener = null;

    fragment.onLocationButtonClick();

    assertSame(location, fragment.location);
  }

  @Test
  public void testOnPostButtonClick() {
    final String text = "text";
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText(text);
    assertNotNull(fragment.location);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadFile(any());

    spy.onPostButtonClick();

    verify(spy).uploadFile(text);
  }

  @Test
  public void testOnPostButtonClickInvalidImageUrl() {
    fragment.imageUrl = null;
    binding.createPostCaption.setText("text");
    assertNotNull(fragment.location);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadFile(any());

    spy.onPostButtonClick();

    verify(spy, never()).uploadFile(any());
  }

  @Test
  public void testOnPostButtonClickInvalidTextNull() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText(null);
    assertNotNull(fragment.location);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadFile(any());

    spy.onPostButtonClick();

    verify(spy, never()).uploadFile(any());
  }

  @Test
  public void testOnPostButtonClickInvalidTextEmptyString() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("");
    assertNotNull(fragment.location);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadFile(any());

    spy.onPostButtonClick();

    verify(spy, never()).uploadFile(any());
  }

  @Test
  public void testOnPostButtonClickInvalidLocation() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("text");
    fragment.location = null;
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadFile(any());

    spy.onPostButtonClick();

    verify(spy, never()).uploadFile(any());
  }

  @Test
  public void testUploadFileHttpScheme() {
    final String text = "text";
    fragment.imageUrl = "http://www.example.com";
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadPost(any());

    spy.uploadFile(text);

    verify(postRepository, never()).uploadFile(any(), any(), any());
    verify(spy).uploadPost(text);
  }

  @Test
  public void testUploadFileFileScheme() {
    final String fileUrl = "file:///example.jpg";
    fragment.imageUrl = fileUrl;
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).uploadPost(any());

    spy.uploadFile("text");

    verify(postRepository).uploadFile(eq(Uri.parse(fileUrl)), anyString(),
        any(UploadUriListener.class));
    verify(spy, never()).uploadPost(any());
  }

  @Test
  public void testOnFileUploaded() {
    final Uri originalUri = mock(Uri.class);
    final Uri uploadedUri = Uri.parse("http://www.example.com");
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onPostButtonClick();

    spy.onFileUploaded(originalUri, uploadedUri);

    verify(spy).onPostButtonClick();
  }

  @Test
  public void testOnFileUploadedUriNull() {
    final Uri originalUri = mock(Uri.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onPostButtonClick();

    spy.onFileUploaded(originalUri, null);

    verify(spy, never()).onPostButtonClick();
  }

  @Test
  public void testOnFileUploadedUriWrongScheme() {
    final Uri originalUri = mock(Uri.class);
    final Uri uploadedUri = Uri.parse("file:///image.jpg");
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).onPostButtonClick();

    spy.onFileUploaded(originalUri, uploadedUri);

    verify(spy, never()).onPostButtonClick();
  }

  @Test
  public void testUploadPostNew() {
    final String text = "text";
    final String idToken = context.getString(R.string.dummy_id_token);
    final String userId = "userId";
    fragment.location = new LatLng(1.0d, 2.0d);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_new));
    when(googleSignInAccount.getId()).thenReturn(userId);

    fragment.uploadPost(text);

    verify(postRepository)
        .newMessage(eq(idToken), eq(userId), eq(1.0d), eq(2.0d), eq(imageUrl), eq(text), any(),
            any());
  }

  @Test
  public void testUploadPostUpdate() {
    final String text = "text";
    final String idToken = context.getString(R.string.dummy_id_token);
    final String userId = "userId";
    fragment.location = new LatLng(1.0d, 2.0d);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_update));
    when(googleSignInAccount.getId()).thenReturn(userId);
    final Message message =
        new Message("id", userId, imageUrl, text, location.latitude, location.longitude,
            "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);

    fragment.uploadPost(text);

    verify(postRepository)
        .updateMessage(eq(idToken), eq(message.getId()), eq(userId), eq(1.0d), eq(2.0d),
            eq(imageUrl), eq(text), any(), any());
  }

  @Test
  public void testUploadPostInvalidArgument() {
    final String text = "text";
    final String idToken = context.getString(R.string.dummy_id_token);
    final String userId = "userId";
    fragment.location = new LatLng(1.0d, 2.0d);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(null);
    when(googleSignInAccount.getId()).thenReturn(userId);

    assertThrows(IllegalArgumentException.class, () -> fragment.uploadPost(text));
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testOnDeleteButtonClick() {
    final FragmentManager fragmentManager = mock(FragmentManager.class);
    final PostFragment spy = spy(fragment);
    doReturn(fragmentManager).when(spy).getChildFragmentManager();
    final FragmentTransaction transaction = mock(FragmentTransaction.class);
    when(fragmentManager.beginTransaction()).thenReturn(transaction);

    spy.onDeleteButtonClick();

    // Confirm method completes.
  }

  @Test
  public void testOnBooleanResultTrue() {
    final String requestKey = "requestKey";
    final Bundle result = mock(Bundle.class);
    final String idToken = context.getString(R.string.dummy_id_token);
    when(result.getBoolean(anyString())).thenReturn(true);
    final String userId = "userId";
    when(googleSignInAccount.getId()).thenReturn(userId);
    final Message message =
        new Message("id", "ignoreUserId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);

    fragment.onBooleanResult(requestKey, result);

    verify(postRepository)
        .deleteMessage(eq(idToken), eq("id"), eq("userId"), any(),
            any());
  }

  @Test
  public void testOnBooleanResultFalse() {
    final String requestKey = "requestKey";
    final Bundle result = mock(Bundle.class);
    when(result.getBoolean(anyString())).thenReturn(false);
    final String userId = "userId";
    when(googleSignInAccount.getId()).thenReturn(userId);
    final Message message =
        new Message("id", "ignoreUserId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);

    fragment.onBooleanResult(requestKey, result);

    verify(postRepository, never()).deleteMessage(any(), any(), any(), any(), any());
  }

  @Test(expected = AssertionError.class)
  public void testOnBooleanResultMessageNotPresent() {
    final String requestKey = "requestKey";
    final Bundle result = mock(Bundle.class);
    when(result.getBoolean(anyString())).thenReturn(true);
    final String userId = "userId";
    when(googleSignInAccount.getId()).thenReturn(userId);
    final Message message =
            new Message("id", "ignoreUserId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(null);

    fragment.onBooleanResult(requestKey, result);

    verify(postRepository, never()).deleteMessage(any(), any(), any(), any(), any());
  }

  @Test(expected = AssertionError.class)
  public void testOnBooleanResultUserIdNull() {
    final String requestKey = "requestKey";
    final Bundle result = mock(Bundle.class);
    when(result.getBoolean(anyString())).thenReturn(true);
    final String userId = "userId";
    when(googleSignInAccount.getId()).thenReturn(null);
    final Message message =
            new Message("id", "ignoreUserId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(null);

    fragment.onBooleanResult(requestKey, result);

    verify(postRepository, never()).deleteMessage(any(), any(), any(), any(), any());
  }

  @Test
  public void testOnNewMessageResponse() {
    NewMessageResponse response = mock(NewMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onNewMessageResponse(response);

    verify(spy).closeFragment();
  }

  @Test
  public void testOnUpdateMessageResponse() {
    UpdateMessageResponse response = mock(UpdateMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onUpdateMessageResponse(response);

    verify(spy).closeFragment();
  }

  @Test
  public void testOnDeleteMessageResponse() {
    DeleteMessageResponse response = mock(DeleteMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onDeleteMessageResponse(response);

    verify(spy).closeFragment();
  }

  @Test
  public void testCloseFragment() {
    fragment.closeFragment();

    verify(activity).onBackPressed();
  }

  @Test
  public void testOnNetworkError() {
    VolleyError error = mock(VolleyError.class);

    fragment.onNetworkError(error);

    // Confirm method completes.
  }

}
