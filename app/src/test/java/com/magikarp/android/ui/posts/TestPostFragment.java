package com.magikarp.android.ui.posts;

import static android.os.Looper.getMainLooper;
import static com.magikarp.android.ui.posts.PostFragment.SAVESTATE_IMAGE_URL;
import static com.magikarp.android.ui.posts.PostFragment.SAVESTATE_LOCATION;
import static com.magikarp.android.ui.posts.PostFragment.SAVESTATE_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.Config.OLDEST_SDK;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


import android.content.ContentResolver;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.model.LatLng;
import com.magikarp.android.R;
import com.magikarp.android.data.PostRepository;
import com.magikarp.android.data.model.DeleteMessageResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.data.model.UpdateMessageResponse;
import com.magikarp.android.databinding.FragmentPostBinding;
import com.magikarp.android.services.LocationService;
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Class for testing {@code PostFragment}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = OLDEST_SDK)
@LooperMode(PAUSED)
public class TestPostFragment {

  private final String imageUrl = "imageUrl";

  @Mock
  private Bundle arguments;
  @Mock
  private LatLng location;
  @Mock
  private LocationService locationService;
  @Mock
  private ServiceConnection serviceConnection;
  @Mock
  ContentResolver contentResolver;
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
        new PostFragment(arguments, context, binding, location, locationService, serviceConnection,
            imageUrl, contentResolver, imageLoader, postRepository, requestQueue);
  }

  @After
  public void teardown() throws Exception {
    closeable.close();
  }

  @Test
  public void testPerformOnCreate() {
    assertFalse(fragment.hasOptionsMenu());

    fragment.performOnCreate();
    shadowOf(getMainLooper()).idle();

    assertTrue(fragment.hasOptionsMenu());
    verify(serviceConnection).onServiceConnected(Mockito.any(), Mockito.any());
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
    doNothing().when(spy).onGpsButtonClick();

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

    assertTrue(spy.onOptionsItemSelected(item));
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
    doNothing().when(spy).loadImage(Mockito.anyString());

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
    doNothing().when(spy).loadImage(Mockito.anyString());

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
    doNothing().when(spy).loadImage(Mockito.anyString());

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
    when(savedInstanceState.getParcelable(SAVESTATE_LOCATION)).thenReturn(latLng);
    when(savedInstanceState.getString(SAVESTATE_IMAGE_URL)).thenReturn("imageUrl");
    when(savedInstanceState.getString(SAVESTATE_TEXT)).thenReturn("text");
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).loadImage(Mockito.anyString());

    spy.onViewCreated(view, savedInstanceState);

    assertSame(latLng, spy.location);
    assertSame("imageUrl", spy.imageUrl);
    assertEquals("text", spy.binding.createPostCaption.getText().toString());
  }

  @Test
  public void testOnSaveInstanceStatePostTypeNew() {
    final Bundle bundle = mock(Bundle.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_new));

    fragment.onSaveInstanceState(bundle);

    verify(bundle).putParcelable(Mockito.anyString(), Mockito.any());
  }

  @Test
  public void testOnSaveInstanceStatePostTypeUpdate() {
    final Bundle bundle = mock(Bundle.class);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_update));

    fragment.onSaveInstanceState(bundle);

    verify(bundle).putParcelable(Mockito.anyString(), Mockito.any());
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

    verify(serviceConnection).onServiceDisconnected(Mockito.any());
    verify(locationService).dispose();
  }
//
//  @Test
//  public void testPerformOnActivityResult() {
//    Intent mockData = mock(Intent.class);
//
//    PostFragment postFragment = new PostFragment();
//
//    postFragment.performOnActivityResult(
//        PostFragment.RESULT_LOAD_IMG - 1,
//        Activity.RESULT_CANCELED,
//        mockData
//    );
//
//    verifyNoInteractions(mockData);
//  }

  @Test
  public void testLoadImageHttpSchema() {
    final String imageUrl = "http://example.com/image.png";

    fragment.loadImage(imageUrl);

    assertEquals(View.VISIBLE, binding.createPostNetworkImage.getVisibility());
    assertEquals(View.INVISIBLE, binding.createPostLocalImage.getVisibility());
    verify(imageLoader).get(eq(imageUrl), Mockito.any());
  }

  @Test
  public void testLoadImageFileSchema() {
    final String imageUrl = "file://example.com/image.png";

    fragment.loadImage(imageUrl);

    assertEquals(View.INVISIBLE, binding.createPostNetworkImage.getVisibility());
    assertEquals(View.VISIBLE, binding.createPostLocalImage.getVisibility());
    verifyNoInteractions(imageLoader);
  }

  @Test
  public void testLoadImageFileSchemaFileNotFound() throws FileNotFoundException {
    final String imageUrl = "file://example.com/image.png";
    doThrow(FileNotFoundException.class).when(contentResolver).openInputStream(notNull());

    fragment.loadImage(imageUrl);

    // Confirm method completes.
  }

//  @Test
//  public void testOnGpsButtonClick() {
//    MenuItem mockMenuItem = mock(MenuItem.class);
//
//    PostFragment postFragment = new PostFragment(null, null, false);
//
//    postFragment.onGpsButtonClick(mockMenuItem);
//
//    verifyNoInteractions(mockMenuItem);
//  }
//
//  @Test
//  public void testPerformOnRequestPermissionsResult() {
//    Context context = mock(Context.class);
//
//    PostFragment postFragment = new PostFragment();
//
//    postFragment.performOnRequestPermissionsResult(context, 0, new int[] {});
//
//    verifyNoInteractions(context);
//  }

  @Test
  public void testOnPostButtonClickInvalidImageUrl() {
    fragment.imageUrl = null;
    binding.createPostCaption.setText("text");
    assertNotNull(fragment.location);

    fragment.onPostButtonClick();

    verifyNoInteractions(postRepository);
  }

  @Test
  public void testOnPostButtonClickInvalidTextNull() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText(null);
    assertNotNull(fragment.location);

    fragment.onPostButtonClick();

    verifyNoInteractions(postRepository);
  }

  @Test
  public void testOnPostButtonClickInvalidTextEmptyString() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("");
    assertNotNull(fragment.location);

    fragment.onPostButtonClick();

    verifyNoInteractions(postRepository);
  }

  @Test
  public void testOnPostButtonClickInvalidLocation() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("text");
    fragment.location = null;

    fragment.onPostButtonClick();

    verifyNoInteractions(postRepository);
  }

  @Test
  public void testOnPostButtonClickNew() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("text");
    assertNotNull(fragment.location);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_new));

    fragment.onPostButtonClick();

    verify(postRepository)
        .newMessage(Mockito.anyString(), eq(location.latitude), eq(location.longitude),
            Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
  }

  @Test
  public void testOnPostButtonClickUpdate() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("text");
    assertNotNull(fragment.location);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_update));

    fragment.onPostButtonClick();

    verify(postRepository)
        .updateMessage(Mockito.anyString(), Mockito.anyString(), eq(location.latitude),
            eq(location.longitude), Mockito.anyString(), Mockito.anyString(), Mockito.any(),
            Mockito.any());
  }

  @Test
  public void testOnPostButtonClickView() {
    assertNotNull(fragment.imageUrl);
    binding.createPostCaption.setText("text");
    assertNotNull(fragment.location);
    when(arguments.getString(context.getString(R.string.args_post_type)))
        .thenReturn(context.getString(R.string.arg_post_type_view));

    assertThrows(IllegalArgumentException.class, () -> fragment.onPostButtonClick());
  }

  @Test
  public void testOnDeleteButtonClick() {
    final Message message =
        new Message("id", "userId", "imageUrl", "text", 1.0d, 2.0d, "timestamp");
    when(arguments.getParcelable(context.getString(R.string.args_message))).thenReturn(message);

    fragment.onDeleteButtonClick();

    verify(postRepository).deleteMessage(eq("id"), eq("userId"), Mockito.any(), Mockito.any());
  }

  @Test
  public void testOnNewMessageResponse() {
    NewMessageResponse response = mock(NewMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onNewMessageResponse(response);

    // Confirm method completes.
  }

  @Test
  public void testOnUpdateMessageResponse() {
    UpdateMessageResponse response = mock(UpdateMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onUpdateMessageResponse(response);

    // Confirm method completes.
  }

  @Test
  public void testOnDeleteMessageResponse() {
    DeleteMessageResponse response = mock(DeleteMessageResponse.class);
    final PostFragment spy = spy(fragment);
    doNothing().when(spy).closeFragment();

    spy.onDeleteMessageResponse(response);

    // Confirm method completes.
  }

  @Test
  public void testOnNetworkError() {
    VolleyError error = mock(VolleyError.class);

    fragment.onNetworkError(error);

    // Confirm method completes.
  }

}
