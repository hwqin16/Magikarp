package com.magikarp.android.ui.posts;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.magikarp.android.R;
import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.data.model.GetMessagesRequest;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.NewMessageRequest;
import com.magikarp.android.network.GsonRequest;
import com.magikarp.android.services.LocationService;
import dagger.hilt.android.AndroidEntryPoint;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;


/**
 * A fragment for viewing and editing posts.
 */
@AndroidEntryPoint
public class PostFragment extends Fragment {

  private static final int RESULT_LOAD_IMG = 1;
  private static final String IMAGE_VIEW_ARGUMENT = "image_view";
  private static final String CONTENT_ARGUMENT = "content";
  private static final String LATITUDE_ARGUMENT = "latitude";
  private static final String LONGITUDE_ARGUMENT = "longitude";
  private static final String TOOLBAR_EXTENSION_ID = "post_toolbar_extension";
  private final String content = null;
  @Inject
  ImageLoader imageLoader;

  private Double latitude = null;
  private Double longitude = null;
  private Bitmap image = null;
  private LocationService gpsService;
  private boolean isGpsServiceBound = false;
  private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
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

    final Intent gpsIntent = new Intent(requireContext(), LocationService.class);
    final Activity activity = requireActivity();
    activity.bindService(gpsIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
    activity.startService(gpsIntent);
  }

  @Override
  public void onDestroy() {
    gpsService.dispose();
    final Activity activity = requireActivity();
    activity.unbindService(gpsServiceConnection);
    super.onDestroy();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    Bundle arguments = getArguments();
    assert arguments != null;
    if (arguments.getBoolean(getResources().getString(R.string.args_is_editable))) {
      inflater.inflate(R.menu.menu_post_edit, menu);
      menu.findItem(R.id.menu_get_location).setOnMenuItemClickListener(this::onGpsButtonClick);
      menu.findItem(R.id.menu_upload_content).setOnMenuItemClickListener(this::onPostButtonClick);
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
    // *****Check that arguments are passed to fragment.*****//
    final Resources resources = getResources();
    final Bundle args = getArguments();
    assert args != null;

    final boolean isReadOnly = !args.getBoolean(resources.getString(R.string.args_is_editable));
    Double argLat = null, argLon = null;
    String argContent = null;
    String imageUrl = args.getString(resources.getString(R.string.args_image_uri));
    final NetworkImageView imageView = view.findViewById(R.id.create_post_image_preview);

    if (isReadOnly) {
      // Read from the arguments
      argContent = args.getString(resources.getString(R.string.args_text));
      if (argContent != null) {
        argLat = args.getDouble(resources.getString(R.string.args_latitude));
        argLon = args.getDouble(resources.getString(R.string.args_longitude));
      }
      if (imageUrl != null) {
        loadImage(imageUrl);
      } else {
        final String dummyImage = "https://i.imgur.com/asvhtNe.jpg";
        loadImage(dummyImage);
      }
    } else {
      if (savedInstanceState != null) {
        image = savedInstanceState.getParcelable(IMAGE_VIEW_ARGUMENT);
        loadImage(image);

        argContent = savedInstanceState.getString(CONTENT_ARGUMENT);
        if (argContent != null) {
          argLat = savedInstanceState.getDouble(LATITUDE_ARGUMENT);
          argLon = savedInstanceState.getDouble(LONGITUDE_ARGUMENT);
        }
      } else {
        argContent = args.getString(resources.getString(R.string.args_text));
        if (argContent != null) {
          argLat = args.getDouble(resources.getString(R.string.args_latitude));
          argLon = args.getDouble(resources.getString(R.string.args_longitude));
        }

        if (imageUrl != null) {
          loadImage(imageUrl);
        } else {
          imageView.setDefaultImageResId(R.drawable.ic_menu_gallery);
          imageView.setErrorImageResId(R.drawable.ic_menu_gallery);
        }
      }
    }

    latitude = argLat;
    longitude = argLon;

    if (!isReadOnly) {
      imageView.setOnClickListener(this::selectImageAction);
    }

    final EditText textContentField = view.findViewById(R.id.create_post_caption);
    textContentField.setText(argContent);
    textContentField.setFocusable(!isReadOnly);
    textContentField.setFocusableInTouchMode(!isReadOnly);
    loadImage(image);

    Log.i("PostFragment",
        "Editable: " + args.getBoolean(resources.getString(R.string.args_is_editable)));
    Log.i("PostFragment", "lat: " + args.getDouble(resources.getString(R.string.args_latitude)));
    Log.i("PostFragment", "long: " + args.getDouble(resources.getString(R.string.args_longitude)));
    Log.i("PostFragment", "text: " + args.getString(resources.getString(R.string.args_text)));
    Log.i("PostFragment", "URI: " + args.getString(resources.getString(R.string.args_image_uri)));
  }

  @Override
  public void onSaveInstanceState(final Bundle bundle) {
    bundle.putParcelable(IMAGE_VIEW_ARGUMENT, image);
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

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[],
                                         int[] grantResults) {
    final Activity activity = requireActivity();

    if (requestCode == 1) {
      if (!(grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        Toast.makeText(activity, "Permission denied to access GPS location",
            Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(activity, "Permission granted to access GPS location",
            Toast.LENGTH_SHORT).show();
        longitude = gpsService.getLocation().getLongitude();
        latitude = gpsService.getLocation().getLatitude();
      }
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
   * @param imageUri image to load
   */
  private void loadImage(final Uri imageUri) {
    try {
      final Context context = requireContext();

      final InputStream imageInput = context.getContentResolver().openInputStream(imageUri);
      final Bitmap selectedImage = BitmapFactory.decodeStream(imageInput);
      loadImage(selectedImage);
    } catch (final FileNotFoundException e) {
      Log.e("onActivityResult", "Failed to load image.", e);
    }
  }

  /**
   * Loads an image from a URL into the image view.
   *
   * @param imageUrl image to load
   */
  private void loadImage(final String imageUrl) {
    final Activity activity = requireActivity();
    final NetworkImageView preview = activity.findViewById(R.id.create_post_image_preview);
    imageLoader.get(imageUrl, ImageLoader
        .getImageListener(preview, R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round));
    preview.setImageUrl(imageUrl, imageLoader);
  }

  /**
   * Loads an image into the image view.
   *
   * @param image image to load
   */
  private void loadImage(final Bitmap image) {
    final Activity activity = requireActivity();
    final Bitmap selectedImage = image;
    final NetworkImageView preview = activity.findViewById(R.id.create_post_image_preview);
    preview.setImageBitmap(selectedImage);

    this.image = selectedImage;
  }

  /**
   * GPS button click callback.
   *
   * @param item item clicked
   * @return always {@code true}
   */
  private boolean onGpsButtonClick(final MenuItem item) {
    if (isGpsServiceBound) {
      if (gpsService.isLocationEnabled()) {
        final Location location = gpsService.getLocation();
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        Log.d("onGpsButtonClick", "Found GPS Location: " + latitude + ", " + longitude);
      } else {
        final Activity activity = requireActivity();
        if (ActivityCompat
            .checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(activity,
              new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
          Log.d("onGpsButtonClick", "GPS service not accessible.");
        }
      }
    } else {
      Log.d("onGpsButtonClick", "GPS service not bound.");
    }
    return true;
  }

  /**
   * Post button click callback
   *
   * @param item item clicked
   * @return always {@code true}
   */
  private boolean onPostButtonClick(final MenuItem item) {

    if (latitude == null && longitude == null) {
      // GPS position wasn't fetched. Fetch it.
      onGpsButtonClick(item);
      Log.i("onPostButtonClick", "No location selected. Grabbing the location now.");
    }

    if (longitude == null && latitude == null) {
      Toast.makeText(requireActivity(), "Permission denied to access GPS location", Toast.LENGTH_SHORT)
          .show();
    }

    UUID imageName = UUID.randomUUID();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    StorageReference ref = storageRef.child("images/" + imageName.toString() + ".png");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] data = baos.toByteArray();

    UploadTask uploadTask = ref.putBytes(data);
    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
      @Override
      public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
        if (!task.isSuccessful()) {
          throw task.getException();
        }

        // Continue with the task to get the download URL
        return ref.getDownloadUrl();
      }
    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
      @Override
      public void onComplete(@NonNull Task<Uri> task) {
        if (task.isSuccessful()) {
          Uri downloadUri = task.getResult();
          // Create message body
          final NewMessageRequest body = new NewMessageRequest(downloadUri.toString(), "test", latitude, longitude);
          // Create a new GSON request.
          GsonRequest<GetMessagesResponse> request =
                  new GsonRequest<>(Request.Method.POST, url, GetMessagesResponse.class,
                          new Gson().toJson(body), new MapsRepository.GetMessagesResponseListener(listener), errorListener);
          requestQueue.add(request);

        } else {
          // Handle failures
          // ...
        }
      }
    });




   return true;
  }
}
