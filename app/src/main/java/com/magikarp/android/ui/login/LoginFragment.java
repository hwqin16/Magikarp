package com.magikarp.android.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.magikarp.android.R;

public class LoginFragment extends Fragment {

  private LoginViewModel loginViewModel;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
        .get(LoginViewModel.class);

    final EditText usernameEditText = view.findViewById(R.id.username);
    final EditText passwordEditText = view.findViewById(R.id.password);
    final Button loginButton = view.findViewById(R.id.login);
    final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

    loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), loginFormState -> {
      if (loginFormState == null) {
        return;
      }
      loginButton.setEnabled(loginFormState.isDataValid());
      if (loginFormState.getUsernameError() != null) {
        usernameEditText.setError(getString(loginFormState.getUsernameError()));
      }
      if (loginFormState.getPasswordError() != null) {
        passwordEditText.setError(getString(loginFormState.getPasswordError()));
      }
    });

    loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
      if (loginResult == null) {
        return;
      }
      loadingProgressBar.setVisibility(View.GONE);
      if (loginResult.getError() != null) {
        showLoginFailed(loginResult.getError());
      }
      if (loginResult.getSuccess() != null) {
        updateUiWithUser(loginResult.getSuccess());
      }
    });

    TextWatcher afterTextChangedListener = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // ignore
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // ignore
      }

      @Override
      public void afterTextChanged(Editable s) {
        loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
            passwordEditText.getText().toString());
      }
    };
    usernameEditText.addTextChangedListener(afterTextChangedListener);
    passwordEditText.addTextChangedListener(afterTextChangedListener);
    passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        loginViewModel.login(usernameEditText.getText().toString(),
            passwordEditText.getText().toString());
      }
      return false;
    });

    loginButton.setOnClickListener(v -> {
      loadingProgressBar.setVisibility(View.VISIBLE);
      loginViewModel.login(usernameEditText.getText().toString(),
          passwordEditText.getText().toString());
    });
  }

  private void updateUiWithUser(LoggedInUserView model) {
    String welcome = getString(R.string.welcome) + model.getDisplayName();
    // TODO : initiate successful logged in experience
    if (getContext() != null && getContext().getApplicationContext() != null) {
      Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }
  }

  private void showLoginFailed(@StringRes Integer errorString) {
    if (getContext() != null && getContext().getApplicationContext() != null) {
      Toast.makeText(
          getContext().getApplicationContext(),
          errorString,
          Toast.LENGTH_LONG).show();
    }
  }
}