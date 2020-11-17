package com.magikarp.android.ui.login;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.magikarp.android.R;
import com.magikarp.android.data.LoginRepository;
import com.magikarp.android.data.Result;
import com.magikarp.android.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {

  private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
  private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
  private LoginRepository loginRepository;

  LoginViewModel(LoginRepository loginRepository) {
    this.loginRepository = loginRepository;
  }

  LiveData<LoginFormState> getLoginFormState() {
    return loginFormState;
  }

  LiveData<LoginResult> getLoginResult() {
    return loginResult;
  }

  /**
   * Log in with login repository.
   *
   * @param username login username
   * @param password login password
   */
  public void login(String username, String password) {
    // can be launched in a separate asynchronous job
    Result<LoggedInUser> result = loginRepository.login(username, password);

    if (result instanceof Result.Success) {
      LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
      loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
    } else {
      loginResult.setValue(new LoginResult(R.string.login_failed));
    }
  }

  /**
   * Change login data.
   *
   * @param username login username
   * @param password login password
   */
  public void loginDataChanged(String username, String password) {
    if (!isUserNameValid(username)) {
      loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
    } else if (!isPasswordValid(password)) {
      loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
    } else {
      loginFormState.setValue(new LoginFormState(true));
    }
  }

  // A placeholder username validation check
  private boolean isUserNameValid(String username) {
    if (username == null) {
      return false;
    }
    if (username.contains("@")) {
      return Patterns.EMAIL_ADDRESS.matcher(username).matches();
    } else {
      return !username.trim().isEmpty();
    }
  }

  // A placeholder password validation check
  private boolean isPasswordValid(String password) {
    return password != null && password.trim().length() > 5;
  }
}