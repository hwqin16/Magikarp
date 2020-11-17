package auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Authenticates that a passed GoogleIdToken is valid.
 */
public class GoogleIdTokenAuthenticator {

  private final GoogleIdTokenVerifier verifier;

  public GoogleIdTokenAuthenticator(GoogleIdTokenVerifier verifier) {
    this.verifier = verifier;
  }

  /**
   * Get UserId string from the encoded idTokenString.
   *
   * @param idTokenString String encoded sent from Google containing UserId
   * @return String UserId encoded by the idTokenString
   * @throws GeneralSecurityException If a security exception occurs
   * @throws IOException              If the id can't be properly parsed
   * @throws InvalidTokenException    If the id parses to an invalid user
   */
  public String getUserId(String idTokenString)
      throws GeneralSecurityException, IOException, InvalidTokenException {
    GoogleIdToken idToken = verifier.verify(idTokenString);

    if (idToken != null) {
      return idToken.getPayload().getSubject();
    } else {
      throw new InvalidTokenException("Invalid idToken found");
    }
  }
}
