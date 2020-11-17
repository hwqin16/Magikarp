package auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TestGoogleIdTokenAuthenticator {

  @Test
  public void testValidGetUserId()
      throws GeneralSecurityException, IOException, InvalidTokenException {
    String ownerId = "chesterTester";
    String idTokenString = "test123";

    GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
    payload.setSubject(ownerId);

    GoogleIdToken mockToken = mock(GoogleIdToken.class);
    when(mockToken.getPayload()).thenReturn(payload);

    GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
    when(mockVerifier.verify(idTokenString)).thenReturn(mockToken);

    GoogleIdTokenAuthenticator authenticator = new GoogleIdTokenAuthenticator(mockVerifier);

    assertEquals(ownerId, authenticator.getUserId(idTokenString));
  }

  @Test
  public void testInvalidInvalidGetUserId() throws GeneralSecurityException, IOException {
    String idTokenString = "test456";

    GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
    when(mockVerifier.verify(idTokenString)).thenReturn(null);

    GoogleIdTokenAuthenticator authenticator = new GoogleIdTokenAuthenticator(mockVerifier);

    assertThrows(InvalidTokenException.class, () -> authenticator.getUserId(idTokenString));
  }
}
