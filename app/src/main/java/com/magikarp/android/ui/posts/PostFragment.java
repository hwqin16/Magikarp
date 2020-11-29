package com.magikarp.android.ui.posts;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.model.LatLng;
import com.magikarp.android.R;
import com.magikarp.android.data.PostRepository;
import com.magikarp.android.data.model.DeleteMessageResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.data.model.UpdateMessageResponse;
import com.magikarp.android.databinding.FragmentPostBinding;
import com.magikarp.android.services.LocationService;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * A fragment for viewing and editing posts.
 */
@AndroidEntryPoint
public class PostFragment extends Fragment {

  @VisibleForTesting
  static final int RESULT_LOAD_IMG = 1;
  @VisibleForTesting
  static final String SAVESTATE_IMAGE_URL = "imageUrl";
  @VisibleForTesting
  static final String SAVESTATE_TEXT = "text";
  @VisibleForTesting
  static final String SAVESTATE_LOCATION = "location";
  @VisibleForTesting
  static final String URI_SCHEME_HTTP = "http";

  private boolean isGpsServiceBound = false;

  private Bundle arguments;

  private Context context;
  @VisibleForTesting
  FragmentPostBinding binding;
  @VisibleForTesting
  LatLng location;

  private LocationService gpsService;

  private ServiceConnection gpsServiceConnection;
  @VisibleForTesting
  String imageUrl;

  @Inject
  ContentResolver contentResolver;
  @Inject
  ImageLoader imageLoader;
  @Inject
  PostRepository postRepository;
  @Inject
  RequestQueue requestQueue;

  /**
   * Default constructor.
   */
  public PostFragment() {
  }

  /**
   * PostFragment constructor for testing.
   *
   * @param arguments       test variable
   * @param context         test variable
   * @param binding         test variable
   * @param location        test variable
   * @param gpsService      test variable
   * @param imageUrl        test variable
   * @param contentResolver test variable
   * @param imageLoader     test variable
   * @param postRepository  test variable
   * @param requestQueue    test variable
   */
  @VisibleForTesting
  PostFragment(
      Bundle arguments, Context context, FragmentPostBinding binding, LatLng location,
      LocationService gpsService, ServiceConnection serviceConnection, String imageUrl,
      ContentResolver contentResolver, ImageLoader imageLoader, PostRepository postRepository,
      RequestQueue requestQueue) {
    this.arguments = arguments;
    this.context = context;
    this.binding = binding;
    this.location = location;
    this.gpsService = gpsService;
    this.gpsServiceConnection = serviceConnection;
    this.imageUrl = imageUrl;
    this.contentResolver = contentResolver;
    this.imageLoader = imageLoader;
    this.postRepository = postRepository;
    this.requestQueue = requestQueue;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    arguments = requireArguments();
    context = requireContext();
    gpsServiceConnection = new GpsServiceConnection();
    performOnCreate();
  }

  @VisibleForTesting
  void performOnCreate() {
    setHasOptionsMenu(true);
    final Intent intent = new Intent(context, LocationService.class);
    context.bindService(intent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
    context.startService(intent);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    if (context.getString(R.string.arg_post_type_new).equals(postType)) {
      inflater.inflate(R.menu.menu_post_new, menu);
    } else if (context.getString(R.string.arg_post_type_update).equals(postType)) {
      inflater.inflate(R.menu.menu_post_update, menu);
    } else if (context.getString(R.string.arg_post_type_view).equals(postType)) {
      inflater.inflate(R.menu.menu_post_view, menu);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.menu_get_location) {
      onGpsButtonClick();
      return true;
    } else if (itemId == R.id.menu_upload_content) {
      onPostButtonClick();
      return true;
    } else if (itemId == R.id.menu_delete) {
      onDeleteButtonClick();
      return true;
    } else if (itemId == R.id.menu_get_directions) { // TODO
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    binding = FragmentPostBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    // Load data from saved state or passed in arguments.
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    final String postTypeUpdate = context.getString(R.string.arg_post_type_update);
    final String postTypeView = context.getString(R.string.arg_post_type_view);

    String text = null;
    if (savedInstanceState != null) {
      location = savedInstanceState.getParcelable(SAVESTATE_LOCATION);
      imageUrl = savedInstanceState.getString(SAVESTATE_IMAGE_URL);
      text = savedInstanceState.getString(SAVESTATE_TEXT);
    } else if (postType.equals(postTypeUpdate) || postType.equals(postTypeView)) {
      final Message message = arguments.getParcelable(context.getString(R.string.args_message));
      location = new LatLng(message.getLatitude(), message.getLongitude());
      imageUrl = message.getImageUrl();
      text = message.getText();
    }

    // Set up the image and text views.
    final NetworkImageView imageView = binding.createPostNetworkImage;
    imageView.setDefaultImageResId(android.R.drawable.ic_menu_gallery);
    imageView.setErrorImageResId(android.R.drawable.ic_menu_gallery);
    final EditText editText = binding.createPostCaption;
    editText.setText(text);

    // Disable editing if UI is read only.
    if (postType.equals(postTypeView)) {
      editText.setEnabled(false);
      editText.setFocusable(false);
      editText.setFocusableInTouchMode(false);
    } else {
      imageView.setOnClickListener(this::selectImageAction);
    }

    // Load image.
    if (imageUrl != null) {
      loadImage(imageUrl);
    }
  }

  @Override
  public void onSaveInstanceState(@NotNull Bundle bundle) {
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    final String postTypeView = context.getString(R.string.arg_post_type_view);

    if (!postType.equals(postTypeView)) {
      bundle.putParcelable(SAVESTATE_LOCATION, location);
      bundle.putString(SAVESTATE_IMAGE_URL, imageUrl);
      bundle.putString(SAVESTATE_TEXT, binding.createPostCaption.getText().toString());
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    context.unbindService(gpsServiceConnection);
    gpsService.dispose();
    context = null;
    arguments = null;
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    performOnActivityResult(requestCode, resultCode, data);
  }

  @VisibleForTesting
  void performOnActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
      loadImage(data.getData().toString());
    }
  }

  /**
   * Starts the activity to select an image.
   *
   * @param view action source
   */
  private void selectImageAction(final View view) {
    Log.d("selectImageAction", "Entered");
    Intent photoSelectionIntent = new Intent(Intent.ACTION_PICK);
    photoSelectionIntent.setType("image/*");
    startActivityForResult(photoSelectionIntent, RESULT_LOAD_IMG);
  }

  /**
   * Loads an image from a URI into the image view.
   *
   * @param imageUrl image to load
   */
  @VisibleForTesting
  void loadImage(@NonNull String imageUrl) {
    final Uri imageUri = Uri.parse(imageUrl);
    final NetworkImageView networkImageView = binding.createPostNetworkImage;
    final ImageView imageView = binding.createPostLocalImage;

    if (imageUri.getScheme().contains(URI_SCHEME_HTTP)) {
      networkImageView.setVisibility(View.VISIBLE);
      imageView.setVisibility(View.INVISIBLE);
      imageLoader.get(imageUrl, ImageLoader
          .getImageListener(networkImageView, android.R.drawable.ic_menu_gallery,
              android.R.drawable.ic_menu_gallery));
      networkImageView.setImageUrl(imageUrl, imageLoader);
    } else {
      try {
        networkImageView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        final InputStream inputStream = contentResolver.openInputStream(imageUri);
        imageView
            .setImageBitmap((inputStream == null) ? null : BitmapFactory.decodeStream(inputStream));
      } catch (final FileNotFoundException exception) {
        imageView.setImageBitmap(null);
        Toast.makeText(context, context.getString(R.string.failure_load_image), Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  /**
   * GPS button click callback.
   */
  @VisibleForTesting
  void onGpsButtonClick() {
    if (isGpsServiceBound) {
      if (gpsService.isLocationEnabled()) {
        final Location position = gpsService.getLocation();
        location = new LatLng(position.getLatitude(), position.getLongitude());
        Log.d("onGpsButtonClick",
            "Found GPS Location: " + position.getLatitude() + ", " + position.getLongitude());
      } else {
        if (ActivityCompat
            .checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(requireActivity(),
              new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
          Log.d("onGpsButtonClick", "GPS service not accessible.");
        }
      }
    } else {
      Log.d("onGpsButtonClick", "GPS service not bound.");
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                         @NotNull int[] grantResults) {
    performOnRequestPermissionsResult(requireContext(), requestCode, grantResults);
  }

  @VisibleForTesting
  void performOnRequestPermissionsResult(
      Context context,
      int requestCode,
      int[] grantResults
  ) {
    if (requestCode == 1) {
      if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        Toast.makeText(context, "Permission denied to access GPS location",
            Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(context, "Permission granted to access GPS location",
            Toast.LENGTH_SHORT).show();
        Location position = gpsService.getLocation();
        location = new LatLng(position.getLatitude(), position.getLongitude());
      }
    }
  }

  /**
   * Post button click callback.
   */
  @VisibleForTesting
  void onPostButtonClick() { // TODO get user ID and post ID
    // Check for valid input and update post repository.
    final String text = binding.createPostCaption.getText().toString();
    if (imageUrl == null || TextUtils.isEmpty(text) || location == null) {
      Toast
          .makeText(context, context.getString(R.string.failure_fields_missing), Toast.LENGTH_SHORT)
          .show();
    } else if (arguments.getString(context.getString(R.string.args_post_type))
        .equals(context.getString(R.string.arg_post_type_new))) {
      postRepository
          .newMessage("", location.latitude, location.longitude, imageUrl, text,
              this::onNewMessageResponse, this::onNetworkError);
    } else if (arguments.getString(context.getString(R.string.args_post_type))
        .equals(context.getString(R.string.arg_post_type_update))) {
      postRepository.updateMessage("", "", location.latitude, location.longitude, imageUrl, text,
          this::onUpdateMessageResponse, this::onNetworkError);
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Delete a message post from the post repository.
   */
  @VisibleForTesting
  void onDeleteButtonClick() {
    final Message message = arguments.getParcelable(context.getString(R.string.args_message));
    // Message should never be null (spotbugs).
    assert message != null;
    postRepository
        .deleteMessage(message.getId(), message.getUserId(), this::onDeleteMessageResponse,
            this::onNetworkError);
  }

  /**
   * Called when a new message post is successfully created.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onNewMessageResponse(NewMessageResponse response) {
    Toast.makeText(context, context.getString(R.string.success_new_post), Toast.LENGTH_SHORT)
        .show();
    closeFragment();
  }

  /**
   * Called when a message post is successfully updated.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onUpdateMessageResponse(UpdateMessageResponse response) {
    Toast.makeText(context, context.getString(R.string.success_update_post), Toast.LENGTH_SHORT)
        .show();
    closeFragment();
  }

  /**
   * Called when a message post is successfully deleted.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onDeleteMessageResponse(DeleteMessageResponse response) {
    Toast.makeText(context, context.getString(R.string.success_delete_post), Toast.LENGTH_SHORT)
        .show();
    closeFragment();
  }

  /**
   * Close the fragment.
   */
  @VisibleForTesting
  void closeFragment() {
    // TODO close the keyboard if required
    requireActivity().onBackPressed();
  }

  /**
   * Callback for a network error.
   */
  @VisibleForTesting
  void onNetworkError(VolleyError error) {
    Toast.makeText(context, context.getString(R.string.failure_network_error), Toast.LENGTH_SHORT)
        .show();
  }

  @VisibleForTesting
  class GpsServiceConnection implements ServiceConnection {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      final LocationService.LocationServiceBinder binder =
          (LocationService.LocationServiceBinder) service;
      gpsService = binder.getService();
      Log.d("GPS Service Conn", "Connected");
      isGpsServiceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.d("GPS Service Conn", "Disconnected");
      isGpsServiceBound = false;
    }

  }

}
