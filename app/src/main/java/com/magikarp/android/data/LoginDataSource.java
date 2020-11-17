package com.magikarp.android.data;

import com.magikarp.android.data.model.LoggedInUser;
import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

  /**
   * Get login result.
   *
   * @param username login username
   * @param password login password
   * @return a login result
   */
  public Result<LoggedInUser> login(String username, String password) {

    try {
      // TODO: handle loggedInUser authentication
      LoggedInUser fakeUser =
          new LoggedInUser(
              java.util.UUID.randomUUID().toString(),
              "Jane Doe");
      return new Result.Success<>(fakeUser);
    } catch (Exception e) {
      return new Result.Error(new IOException("Error logging in", e));
    }
  }

  public void logout() {
    // TODO: revoke authentication
  }
}