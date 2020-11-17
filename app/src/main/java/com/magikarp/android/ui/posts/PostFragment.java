package com.magikarp.android.ui.posts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.magikarp.android.R;
import com.magikarp.android.services.LocationService;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A fragment for viewing and editing posts.
 */
public class PostFragment extends Fragment {

  private static final int RESULT_LOAD_IMG = 1;
  private static final String IMAGE_VIEW_ARGUMENT = "image_view";
  private static final String CONTENT_ARGUMENT = "content";
  private static final String LATITUDE_ARGUMENT = "latitude";
  private static final String LONGITUDE_ARGUMENT = "longitude";
  private static final String TOOLBAR_EXTENSION_ID = "post_toolbar_extension";

  private double latitude = Double.NEGATIVE_INFINITY;
  private double longitude = Double.NEGATIVE_INFINITY;
  private String imagePath = null;
  private String content = null;

  private LocationService gpsService;
  private boolean isGpsServiceBound = false;
  private ServiceConnection gpsServiceConnection = new ServiceConnection() {
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
  };

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param image   Image to populate with
   * @param content Text to populate with
   * @return A new instance of fragment CreatePostFragment.
   */
  public static PostFragment newInstance(final Bitmap image, final String content) {
    PostFragment fragment = new PostFragment();
    Bundle args = new Bundle();
    args.putParcelable(IMAGE_VIEW_ARGUMENT, image);
    args.putString(CONTENT_ARGUMENT, content);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    final Intent gpsIntent = new Intent(getContext(), LocationService.class);
    getActivity().bindService(gpsIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
    getActivity().startService(gpsIntent);
  }

  @Override
  public void onDestroy() {
    gpsService.dispose();
    getActivity().unbindService(gpsServiceConnection);
    super.onDestroy();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    if (getArguments().getBoolean(getResources().getString(R.string.args_is_editable))) {
      inflater.inflate(R.menu.menu_post_edit, menu);
      menu.findItem(R.id.menu_get_location).setOnMenuItemClickListener(this::onGpsButtonClick);
    } else {
      inflater.inflate(R.menu.menu_post_view, menu);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_post, container, false);
  }

  @Override
  public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
    final ImageView imageView = view.findViewById(R.id.create_post_image_preview);
    imageView.setOnClickListener(this::selectImageAction);

    if (savedInstanceState != null) {
      imagePath = savedInstanceState.getString(IMAGE_VIEW_ARGUMENT);
      content = savedInstanceState.getString(CONTENT_ARGUMENT);
      latitude = savedInstanceState.getDouble(LATITUDE_ARGUMENT);
      longitude = savedInstanceState.getDouble(LONGITUDE_ARGUMENT);

      final EditText textContentField = view.findViewById(R.id.create_post_caption);
      textContentField.setText(content);
      loadImage(new Uri.Builder().path(imagePath).build());
    }
  }

  @Override
  public void onSaveInstanceState(final Bundle bundle) {
    bundle.putString(IMAGE_VIEW_ARGUMENT, imagePath);
    bundle.putString(CONTENT_ARGUMENT, content);
    bundle.putDouble(LONGITUDE_ARGUMENT, longitude);
    bundle.putDouble(LATITUDE_ARGUMENT, latitude);
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == RESULT_LOAD_IMG) {
        loadImage(data.getData());
      } else {
        throw new IllegalStateException("Unexpected value: " + requestCode);
      }
    }
  }

  /**
   * Action to execute on post button press.
   *
   * @param view action source
   */
  private void postAction(final View view) {
    Log.d("postAction", "Entered");
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
   * @param imageUri image to load
   */
  private void loadImage(final Uri imageUri) {
    try {
      final InputStream imageInput = getContext().getContentResolver().openInputStream(imageUri);
      final Bitmap selectedImage = BitmapFactory.decodeStream(imageInput);
      final ImageView preview = getActivity().findViewById(R.id.create_post_image_preview);
      preview.setImageBitmap(selectedImage);

      imagePath = imageUri.getPath();
    } catch (final FileNotFoundException e) {
      Log.e("onActivityResult", "Failed to load image.", e);
    }
  }

  /**
   * GPS button click callback.
   *
   * @param item item clicked
   * @return always {@code true}
   */
  private boolean onGpsButtonClick(final MenuItem item) {
    if (isGpsServiceBound) {
      final Location location = gpsService.getLocation();
      longitude = location.getLongitude();
      latitude = location.getLatitude();
      Log.d("onGpsButtonClick", "Found GPS Location: " + latitude + ", " + longitude);
    }
    return true;
  }

}
