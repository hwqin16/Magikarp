package com.magikarp.android.ui.posts;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.magikarp.android.R;
import com.magikarp.android.data.model.NewMessageRequest;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.network.GsonRequest;
import com.magikarp.android.services.LocationService;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * A fragment for viewing and editing posts.
 */
@AndroidEntryPoint
public class PostFragment extends Fragment {

  private static final int RESULT_LOAD_IMG = 1;
  private static final String SAVESTATE_IMAGE = "imageUri";
  private static final String SAVESTATE_TEXT = "text";
  private static final String SAVESTATE_LATITUDE = "latitude";
  private static final String SAVESTATE_LONGITUDE = "longitude";

  private double latitude;
  private double longitude;
  private Bitmap imageBitmap;
  private String text;

  @Inject
  ImageLoader imageLoader;
  @Inject
  RequestQueue requestQueue;

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

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    final Intent gpsIntent = new Intent(requireContext(), LocationService.class);
    final Context context = requireContext();
    context.bindService(gpsIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
    context.startService(gpsIntent);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    if (requireArguments().getBoolean(getResources().getString(R.string.args_is_editable))) {
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
    final Bundle args = requireArguments();
    String imageUrl = null;

    // Load data from saved state or passed in arguments.
    if (savedInstanceState != null) {
      latitude = savedInstanceState.getDouble(SAVESTATE_LATITUDE);
      longitude = savedInstanceState.getDouble(SAVESTATE_LONGITUDE);
      imageBitmap = savedInstanceState.getParcelable(SAVESTATE_IMAGE);
      text = savedInstanceState.getString(SAVESTATE_TEXT);
    } else {
      latitude = args.getDouble(getString(R.string.args_latitude), Double.NaN);
      longitude = args.getDouble(getString(R.string.args_longitude), Double.NaN);
      imageUrl = args.getString(getString(R.string.args_image_uri));
      text = args.getString(getString(R.string.args_text));
    }

    // Set up the image and text views.
    final NetworkImageView imageView = view.findViewById(R.id.create_post_image_preview);
    imageView.setDefaultImageResId(R.drawable.ic_menu_gallery);
    imageView.setErrorImageResId(R.drawable.ic_menu_gallery);
    final EditText editText = view.findViewById(R.id.create_post_caption);
    editText.setText(text);

    // Disable editing if UI is read only.
    if (!args.getBoolean(getString(R.string.args_is_editable))) {
      editText.setEnabled(false);
      editText.setFocusable(false);
      editText.setFocusableInTouchMode(false);
    } else {
      imageView.setOnClickListener(this::selectImageAction);
    }

    // Load image.
    if (imageBitmap != null) {
      loadImage(imageBitmap);
    } else if (imageUrl != null) {
      loadImage(imageUrl);
    }
  }

  @Override
  public void onSaveInstanceState(@NotNull Bundle bundle) {
    if (requireArguments().getBoolean(getString(R.string.args_is_editable))) {
      bundle.putDouble(SAVESTATE_LATITUDE, latitude);
      bundle.putDouble(SAVESTATE_LONGITUDE, longitude);
      bundle.putParcelable(SAVESTATE_IMAGE, imageBitmap);
      bundle.putString(SAVESTATE_TEXT, text);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    gpsService.dispose();
    requireContext().unbindService(gpsServiceConnection);
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
      loadImage(data.getData());
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
  private void loadImage(@NonNull Uri imageUri) {
    try {
      final InputStream imageInput =
          requireContext().getContentResolver().openInputStream(imageUri);
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
  private void loadImage(@NonNull String imageUrl) {
    View view = getView();
    if (view != null) {
      final NetworkImageView preview = view.findViewById(R.id.create_post_image_preview);
      imageLoader.get(imageUrl, ImageLoader
              .getImageListener(preview, R.drawable.ic_menu_gallery, R.drawable.ic_menu_gallery));
      preview.setImageUrl(imageUrl, imageLoader);
    }
  }

  /**
   * Loads an image into the image view.
   *
   * @param image image to load
   */
  private void loadImage(@NonNull Bitmap image) {
    View view = getView();
    if (view != null) {
      final NetworkImageView preview = view.findViewById(R.id.create_post_image_preview);
      preview.setImageBitmap(image);
      this.imageBitmap = image;
    }

  }

  /**
   * GPS button click callback.
   *
   * @param item item clicked
   * @return always {@code true}
   */
  private boolean onGpsButtonClick(@NonNull MenuItem item) {
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                         @NotNull int[] grantResults) {
    final Context context = requireContext();

    if (requestCode == 1) {
      if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        Toast.makeText(context, "Permission denied to access GPS location",
            Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(context, "Permission granted to access GPS location",
            Toast.LENGTH_SHORT).show();
        Location location = gpsService.getLocation();
        longitude = location.getLongitude();
        latitude = location.getLatitude();
      }
    }
  }

  /**
   * Post button click callback.
   *
   * @param item item clicked
   * @return always {@code true}
   */
  private boolean onPostButtonClick(final MenuItem item) {
    if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
      // GPS position wasn't fetched. Fetch it.
      onGpsButtonClick(item);
      Log.i("onPostButtonClick", "No location selected. Grabbing the location now.");
    }
    // Recheck location.
    if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
      Toast.makeText(requireActivity(), "Permission denied to access GPS location",
          Toast.LENGTH_SHORT).show();
    }

    final EditText editText = getView().findViewById(R.id.create_post_caption);

    if (imageBitmap == null || editText.getText().toString().isEmpty() ||  Double.isNaN(latitude)
            || Double.isNaN(longitude)) {
      Toast.makeText(requireActivity(), "One or more fields missing",
              Toast.LENGTH_SHORT).show();
      return true;
    }

    UUID imageName = UUID.randomUUID();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    StorageReference ref = storageRef.child("images/" + imageName.toString() + ".png");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] data = baos.toByteArray();
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
    String id = "noID";
    if (account != null) {
      id = account.getId();
    }
    final String idfinal = id;
    UploadTask uploadTask = ref.putBytes(data);
    uploadTask.continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      // Continue with the task to get the download URL
      return ref.getDownloadUrl();
    }).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        Uri downloadUri = task.getResult();
        // Create message body
        final NewMessageRequest body =
            new NewMessageRequest(downloadUri.toString(), editText.getText().toString(),
                    latitude, longitude);

        String url = getContext().getResources().getString(R.string.server_url)
            + "/messages/" + idfinal + "/new";
        // Create a new GSON request.
        GsonRequest<NewMessageResponse> request =
            new GsonRequest<>(Request.Method.POST, url, NewMessageResponse.class,
                new Gson().toJson(body), response -> {
            }, error -> {
            });
        requestQueue.add(request);
        Toast.makeText(requireActivity(), "Posted!",
                Toast.LENGTH_SHORT).show();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), 0);
        getParentFragmentManager().popBackStackImmediate();


      }
    });

    return true;
  }

}
