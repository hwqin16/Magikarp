package com.magikarp.android.ui.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.DialogFragment;
import com.magikarp.android.R;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

/**
 * A dialog for confirming deletion of a post message.
 */
@AndroidEntryPoint
public class BooleanResponseDialogFragment extends DialogFragment {

  @VisibleForTesting
  Context context;

  /**
   * Default constructor.
   */
  public BooleanResponseDialogFragment() {
  }

  /**
   * Constructor for testing.
   *
   * @param context test variable
   */
  @VisibleForTesting
  BooleanResponseDialogFragment(Context context) {
    this.context = context;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    performOnCreate();
  }

  @VisibleForTesting
  void performOnCreate() {
    // For unit testing.
    context = requireContext();
  }

  @NotNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage("Delete post?");
    builder.setPositiveButton("Delete", this::onPositiveButtonClick);
    builder.setNegativeButton("Cancel", this::onNegativeButtonClick);
    return builder.create();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    context = null;
  }

  @VisibleForTesting
  void onPositiveButtonClick(DialogInterface dialog, int id) {
    setDialogResult(true);
  }

  @VisibleForTesting
  void onNegativeButtonClick(DialogInterface dialog, int id) {
    setDialogResult(false);
  }

  @VisibleForTesting
  void setDialogResult(boolean isDeleted) {
    final String requestKey = context.getString(R.string.dialog_result);
    final Bundle bundle = new Bundle();
    bundle.putBoolean(requestKey, isDeleted);
    getParentFragmentManager().setFragmentResult(requestKey, bundle);
  }

}
