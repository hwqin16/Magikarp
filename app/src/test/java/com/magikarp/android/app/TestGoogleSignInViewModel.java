package com.magikarp.android.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.magikarp.android.ui.app.GoogleSignInViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code GoogleSignInViewModel}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGoogleSignInViewModel {

  @Mock
  MutableLiveData<GoogleSignInAccount> liveData;

  private GoogleSignInViewModel viewModel;

  @Before
  public void setup() {
    viewModel = new GoogleSignInViewModel(liveData);
  }

  @Test
  public void testGetSignedInAccount() {
    assert (viewModel.getSignedInAccount() == liveData);
  }

  @Test
  public void testSetAccount() {
    GoogleSignInAccount account = mock(GoogleSignInAccount.class);

    viewModel.setAccount(account);

    verify(liveData).setValue(account);
  }

}
