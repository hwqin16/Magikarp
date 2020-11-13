package com.magikarp.android.ui.posts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.magikarp.android.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreatePostFragment.
     */
    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param image Image to populate with
     * @param content Text to populate with
     *
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_post_content, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        final ImageView imageView = view.findViewById(R.id.create_post_image_preview);
        imageView.setOnClickListener(this::selectImageAction);

        // final Button postButton = getLayoutInflater().inflate(R.layout.fragment_post_toolbar_extension, null).findViewById(R.id.post_content);
        // postButton.setOnClickListener(this::postAction);

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

    private void postAction(final View view) {
        Log.d("postAction", "Entered");
    }

    private void selectImageAction(final View view) {
        Log.d("selectImageAction", "Entered");
        Intent photoSelectionIntent = new Intent(Intent.ACTION_PICK);
        photoSelectionIntent.setType("image/*");
        startActivityForResult(photoSelectionIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case RESULT_LOAD_IMG: {
                    loadImage(data.getData());
                }
            }
        }
    }

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
}