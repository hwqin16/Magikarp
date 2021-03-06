package com.magikarp.android.ui.posts;

import static com.magikarp.android.util.AssertionUtilities.require;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.GetContent;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.magikarp.android.R;
import com.magikarp.android.data.PostRepository;
import com.magikarp.android.data.model.DeleteMessageResponse;
import com.magikarp.android.data.model.Message;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.data.model.UpdateMessageResponse;
import com.magikarp.android.databinding.FragmentPostBinding;
import com.magikarp.android.location.LocationListener;
import com.magikarp.android.ui.app.BooleanResponseDialogFragment;
import com.magikarp.android.ui.app.GoogleSignInViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * A fragment for viewing and editing posts.
 */
@AndroidEntryPoint
public class PostFragment extends Fragment {

  @VisibleForTesting
  static final String GEO_URL = "google.navigation:q=%f,%f";
  @VisibleForTesting
  static final String MEDIA_TYPE_IMAGE = "image/*";
  @VisibleForTesting
  static final String SAVE_STATE_IMAGE_URL = "imageUrl";
  @VisibleForTesting
  static final String SAVE_STATE_TEXT = "text";
  @VisibleForTesting
  static final String SAVE_STATE_LOCATION = "location";
  @VisibleForTesting
  static final String URI_SCHEME_HTTP = "http";
  @VisibleForTesting
  ActivityResultLauncher<String> requestPermissionLauncher;
  @VisibleForTesting
  ActivityResultLauncher<String> getContentLauncher;
  @VisibleForTesting
  Bundle arguments;
  @VisibleForTesting
  Context context;
  @VisibleForTesting
  FragmentActivity activity;
  @VisibleForTesting
  FragmentPostBinding binding;
  @VisibleForTesting
  GoogleSignInAccount googleSignInAccount;
  @VisibleForTesting
  LatLng location;
  @VisibleForTesting
  LocationListener locationListener;
  @VisibleForTesting
  String imageUrl;
  @Inject
  ContentResolver contentResolver;
  @Inject
  FusedLocationProviderClient fusedLocationClient;
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
   * Constructor for testing.
   *
   * @param getContentLauncher        test variable
   * @param requestPermissionLauncher test variable
   * @param arguments                 test variable
   * @param context                   test variable
   * @param activity                  test variable
   * @param binding                   test variable
   * @param googleSignInAccount       test variable
   * @param location                  test variable
   * @param locationListener          test variable
   * @param imageUrl                  test variable
   * @param contentResolver           test variable
   * @param fusedLocationClient       test variable
   * @param imageLoader               test variable
   * @param postRepository            test variable
   * @param requestQueue              test variable
   */
  @VisibleForTesting
  PostFragment(
      ActivityResultLauncher<String> getContentLauncher,
      ActivityResultLauncher<String> requestPermissionLauncher,
      @NotNull Bundle arguments,
      Context context,
      FragmentActivity activity,
      FragmentPostBinding binding,
      @NotNull GoogleSignInAccount googleSignInAccount,
      @NotNull LatLng location,
      LocationListener locationListener,
      String imageUrl,
      ContentResolver contentResolver,
      FusedLocationProviderClient fusedLocationClient,
      ImageLoader imageLoader,
      PostRepository postRepository,
      RequestQueue requestQueue) {
    this.getContentLauncher = getContentLauncher;
    this.requestPermissionLauncher = requestPermissionLauncher;
    this.arguments = arguments;
    this.context = context;
    this.activity = activity;
    this.binding = binding;
    this.googleSignInAccount = googleSignInAccount;
    this.location = location;
    this.locationListener = locationListener;
    this.imageUrl = imageUrl;
    this.contentResolver = contentResolver;
    this.fusedLocationClient = fusedLocationClient;
    this.imageLoader = imageLoader;
    this.postRepository = postRepository;
    this.requestQueue = requestQueue;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ***** Add setup that cannot be instantiated with a unit test here. ***** //

    // Set up account listener.
    new ViewModelProvider(requireActivity()).get(GoogleSignInViewModel.class).getSignedInAccount()
        .observe(this, this::onGoogleSignInAccountChanged);
    performOnCreate();
  }

  @VisibleForTesting
  void performOnCreate() {
    // For unit testing.
    activity = requireActivity();
    arguments = requireArguments();
    context = requireContext();
    // Set up options menu.
    setHasOptionsMenu(true);
    // Set up fragment to request permissions (i.e. fine location).
    requestPermissionLauncher =
        registerForActivityResult(new RequestPermission(), this::onRequestPermissionResult);
    // Set up fragment to get content (i.e. images).
    getContentLauncher = registerForActivityResult(new GetContent(), this::onGetContentResult);
    // Intercept back button presses to close the soft keyboard.
    activity.getOnBackPressedDispatcher()
        .addCallback(this, new OnBackPressedKeyboardCloser(activity, true));
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
      onLocationButtonClick();
      return true;
    } else if (itemId == R.id.menu_upload_content) {
      onPostButtonClick();
      return true;
    } else if (itemId == R.id.menu_delete) {
      onDeleteButtonClick();
      return true;
    } else if (itemId == R.id.menu_get_directions) {
      final Uri uri =
          Uri.parse(String.format(Locale.US, GEO_URL, location.latitude, location.longitude));
      startActivityFromIntent(new Intent(Intent.ACTION_VIEW, uri));
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    binding = FragmentPostBinding.inflate(inflater, container, false);

    // Disable editing if UI is read only.
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    final String postTypeView = context.getString(R.string.arg_post_type_view);
    if (postType.equals(postTypeView)) {
      final TextInputLayout textInputLayout = binding.createPostCaption;
      textInputLayout.setEnabled(false);
      textInputLayout.setFocusable(false);
      textInputLayout.setFocusableInTouchMode(false);
      textInputLayout.setCounterEnabled(false);
      textInputLayout.setHintAnimationEnabled(false);
      // Correct light gray color of default disabled text.
      binding.editText.setTextColor(context.getResources().getColor(android.R.color.black));
    } else {
      // Enable click listener for selecting an image.
      binding.imageContainer.setOnClickListener(v -> getContentLauncher.launch(MEDIA_TYPE_IMAGE));
    }

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    // Load data from saved state or passed in arguments.
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    final String postTypeUpdate = context.getString(R.string.arg_post_type_update);
    final String postTypeView = context.getString(R.string.arg_post_type_view);

    String text = null;
    String url = null;
    if (savedInstanceState != null) {
      location = savedInstanceState.getParcelable(SAVE_STATE_LOCATION);
      url = savedInstanceState.getString(SAVE_STATE_IMAGE_URL);
      text = savedInstanceState.getString(SAVE_STATE_TEXT);
    } else if (postType.equals(postTypeUpdate) || postType.equals(postTypeView)) {
      final Message message = arguments.getParcelable(context.getString(R.string.args_message));
      location = new LatLng(message.getLatitude(), message.getLongitude());
      url = message.getImageUrl();
      text = message.getText();
    }

    // Set up the image and text views.
    final NetworkImageView imageView = binding.createPostNetworkImage;
    imageView.setDefaultImageResId(R.drawable.background);
    imageView.setErrorImageResId(R.drawable.background_broken_image);
    binding.editText.setText(text);

    // Load image.
    if (url != null) {
      loadImage(url);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // Start location updates.
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      locationListener = new LocationListener();
      fusedLocationClient.requestLocationUpdates(LocationRequest.create(), locationListener, null);
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (locationListener != null) {
      fusedLocationClient.removeLocationUpdates(locationListener);
      locationListener = null;
    }
  }

  @Override
  public void onSaveInstanceState(@NotNull Bundle bundle) {
    bundle.putParcelable(SAVE_STATE_LOCATION, location);
    bundle.putString(SAVE_STATE_IMAGE_URL, imageUrl);
    bundle.putString(SAVE_STATE_TEXT, require(binding.editText.getText()).toString());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    activity = null;
    arguments = null;
    context = null;
  }

  /**
   * Start an activity from an intent.
   *
   * @param intent the intent used to start an activity
   */
  @VisibleForTesting
  void startActivityFromIntent(@NonNull Intent intent) {
    if (intent.resolveActivity(context.getPackageManager()) != null) {
      activity.startActivity(intent);
    } else {
      Toast.makeText(context, context.getString(R.string.failure_no_available_activity),
          Toast.LENGTH_LONG).show();
    }
  }

  /**
   * The result of a "request permission" request.
   *
   * @param result {@code true} if permission granted, {@code false} otherwise
   */
  @VisibleForTesting
  void onRequestPermissionResult(Boolean result) {
    if (result) {
      try {
        locationListener = new LocationListener();
        fusedLocationClient
            .requestLocationUpdates(LocationRequest.create(), locationListener, null);
        return;
      } catch (SecurityException unlikely) {
        fusedLocationClient.removeLocationUpdates(locationListener);
        locationListener = null;
      }
      Toast.makeText(context, context.getString(R.string.failure_fine_location_permission),
          Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Callback for Google Sign-In account changes.
   *
   * @param account the current signed-in account
   */
  @VisibleForTesting
  void onGoogleSignInAccountChanged(@Nullable GoogleSignInAccount account) {
    googleSignInAccount = account;
  }

  /**
   * Get the result of a "get content" request.
   *
   * @param result the result of the request, or {@code null} if there is no result
   */
  @VisibleForTesting
  void onGetContentResult(@Nullable Uri result) {
    if (result != null) {
      loadImage(result.toString());
    }
  }

  /**
   * Loads an image from a URI into the image view.
   *
   * @param imageUrl image to load
   */
  @VisibleForTesting
  void loadImage(@NonNull String imageUrl) {
    this.imageUrl = imageUrl;
    final Uri imageUri = Uri.parse(imageUrl);
    final NetworkImageView networkImageView = binding.createPostNetworkImage;
    final ImageView imageView = binding.createPostLocalImage;

    if (imageUri.getScheme().contains(URI_SCHEME_HTTP)) {
      networkImageView.setVisibility(View.VISIBLE);
      imageView.setVisibility(View.INVISIBLE);
      imageLoader.get(imageUrl, ImageLoader
          .getImageListener(networkImageView, R.drawable.background,
              R.drawable.background_broken_image));
      networkImageView.setImageUrl(imageUrl, imageLoader);
    } else {
      networkImageView.setImageUrl(null, null);
      networkImageView.setVisibility(View.INVISIBLE);
      imageView.setVisibility(View.VISIBLE);
      try {
        final InputStream inputStream = contentResolver.openInputStream(imageUri);
        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
      } catch (FileNotFoundException | SecurityException exception) {
        this.imageUrl = null;
        imageView.setImageResource(R.drawable.background_broken_image);
        Toast.makeText(context, context.getString(R.string.failure_load_image), Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  /**
   * GPS button click callback. By the time this button is available, location permissions should
   * have already been granted.
   */
  @VisibleForTesting
  void onLocationButtonClick() {
    if (locationListener != null) {
      final Location loc = locationListener.getLocation();
      if (loc != null) {
        location = new LatLng(loc.getLatitude(), loc.getLongitude());
        Toast.makeText(context, context.getString(R.string.success_location_updated),
            Toast.LENGTH_SHORT).show();
        return;
      }
    }
    Toast.makeText(context, context.getString(R.string.failure_location_unavailable),
        Toast.LENGTH_LONG).show();
  }

  /**
   * Post button click callback.
   */
  @VisibleForTesting
  void onPostButtonClick() {
    // Check for valid input and update post repository.
    final CharSequence text = binding.editText.getText();
    if (imageUrl != null && !TextUtils.isEmpty(text) && location != null) {
      uploadFile(require(text).toString());
    } else {
      Toast
          .makeText(context, context.getString(R.string.failure_fields_missing), Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * Upload the post image to the server, if required.
   */
  @VisibleForTesting
  void uploadFile(@NonNull String text) {
    // Check for a image URI on local filesystem and upload to server.
    final Uri uri = Uri.parse(imageUrl);
    if (!uri.getScheme().contains(URI_SCHEME_HTTP)) {
      try {
        postRepository.uploadFile(uri, "jpg", this::onFileUploaded);
      } catch (SecurityException exception) {
        binding.createPostLocalImage.setImageResource(R.drawable.background_broken_image);
        Toast.makeText(context, context.getString(R.string.failure_load_image), Toast.LENGTH_LONG)
            .show();
      }
    } else {
      uploadPost(text);
    }
  }

  /**
   * Listener for file upload results from server.
   *
   * @param originalUri original URI of uploaded file
   * @param uploadedUri URI of uploaded file on server
   */
  @VisibleForTesting
  void onFileUploaded(Uri originalUri, Uri uploadedUri) {
    if ((uploadedUri != null) && uploadedUri.getScheme().contains(URI_SCHEME_HTTP)) {
      imageUrl = uploadedUri.toString();
      onPostButtonClick();
    } else {
      Toast.makeText(context, context.getString(R.string.failure_network_error), Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Upload post to server.
   */
  @VisibleForTesting
  void uploadPost(@NonNull String text) {
    final String postType = arguments.getString(context.getString(R.string.args_post_type));
    final String postTypeNew = context.getString(R.string.arg_post_type_new);
    final String postTypeUpdate = context.getString(R.string.arg_post_type_update);
    final String idToken = context.getString(R.string.dummy_id_token);
    final String userId = require(googleSignInAccount.getId());
    if (postTypeNew.equals(postType)) {
      postRepository
          .newMessage(idToken, userId, location.latitude, location.longitude, imageUrl,
              text, this::onNewMessageResponse, this::onNetworkError);
    } else if (postTypeUpdate.equals(postType)) {
      final Message message =
          require(arguments.getParcelable(context.getString(R.string.args_message)));
      postRepository
          .updateMessage(idToken, message.getId(), userId, location.latitude, location.longitude,
              imageUrl, text, this::onUpdateMessageResponse, this::onNetworkError);
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Delete a message post from the post repository.
   */
  @VisibleForTesting
  void onDeleteButtonClick() {
    final DialogFragment fragment = new BooleanResponseDialogFragment();
    final FragmentManager fragmentManager = getChildFragmentManager();
    fragmentManager.setFragmentResultListener(context.getString(R.string.dialog_result), this,
        this::onBooleanResult);
    fragment.show(fragmentManager, null);
  }

  /**
   * Listener for boolean dialog result.
   *
   * @param requestKey key to define the request that started the dialog
   * @param result     dialog result
   */
  @VisibleForTesting
  void onBooleanResult(@NonNull String requestKey, @NonNull Bundle result) {
    final String userId = require(googleSignInAccount.getId());
    final String idToken = context.getString(R.string.dummy_id_token);
    if (result.getBoolean(context.getString(R.string.dialog_result))) {
      final Message message =
          require(arguments.getParcelable(context.getString(R.string.args_message)));
      postRepository.deleteMessage(idToken, message.getId(), userId, this::onDeleteMessageResponse,
          this::onNetworkError);
    }
  }

  /**
   * Called when a new message post is successfully created.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onNewMessageResponse(NewMessageResponse response) {
    Snackbar.make(activity.findViewById(android.R.id.content), R.string.success_new_post,
        Snackbar.LENGTH_SHORT).show();
    activity.onBackPressed();
  }

  /**
   * Called when a message post is successfully updated.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onUpdateMessageResponse(UpdateMessageResponse response) {
    Snackbar.make(activity.findViewById(android.R.id.content), R.string.success_update_post,
        Snackbar.LENGTH_SHORT).show();
    activity.onBackPressed();
  }

  /**
   * Called when a message post is successfully deleted.
   *
   * @param response network response
   */
  @VisibleForTesting
  void onDeleteMessageResponse(DeleteMessageResponse response) {
    Snackbar.make(activity.findViewById(android.R.id.content), R.string.success_delete_post,
        Snackbar.LENGTH_SHORT).show();
    activity.onBackPressed();
  }

  /**
   * Callback for a network error.
   */
  @VisibleForTesting
  void onNetworkError(VolleyError error) {
    Toast.makeText(context, context.getString(R.string.failure_network_error), Toast.LENGTH_LONG)
        .show();
  }

  @VisibleForTesting
  static class OnBackPressedKeyboardCloser extends OnBackPressedCallback {

    private final FragmentActivity activity;

    /**
     * Create a new keyboard closer.
     */
    public OnBackPressedKeyboardCloser(@NonNull FragmentActivity activity, boolean isEnabled) {
      super(isEnabled);
      this.activity = activity;
    }

    @Override
    public void handleOnBackPressed() {
      // Close the soft keyboard.
      final InputMethodManager imm =
          (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getWindowToken(), 0);
      // Reissue back button press to close the fragment.
      setEnabled(false);
      activity.onBackPressed();
    }

  }

}
