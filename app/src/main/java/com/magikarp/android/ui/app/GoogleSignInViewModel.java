package com.magikarp.android.ui.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Class to provide the current signed in user.
 */
public class GoogleSignInViewModel extends ViewModel {

  private final MutableLiveData<GoogleSignInAccount> liveData;

  /**
   * Create a new Google Sign-In view model.
   *
   * @param liveData repository for accessing data
   */
  @ViewModelInject
  public GoogleSignInViewModel(@NonNull MutableLiveData<GoogleSignInAccount> liveData) {
    this.liveData = liveData;
  }

  /**
   * Get a live data object for subscribing to signed-in user updates.
   *
   * @return a live data object for subscribing to signed-in user updates
   */
  @NonNull
  public LiveData<GoogleSignInAccount> getSignedInAccount() {
    return liveData;
  }

  /**
   * Set the signed-in account.
   *
   * @param account the signed-in account
   */
  public void setAccount(@Nullable GoogleSignInAccount account) {
    liveData.setValue(account);
  }

}
