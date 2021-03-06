package constants;

public class Constants {

  private Constants() {
  }

  // TODO use this to build GoogleIdTokenVerifier
  // public static final String OAUTH_CLIENT_ID =
  //    "222054183884-l3lcjqflihkqibfu4s3cu1lk1vhblv5l.apps.googleusercontent.com";
  public static final int PORT = 7000;
  // Everyone will need to create a file like below from
  // https://console.firebase.google.com/u/1/project/magikarp-295201/settings/serviceaccounts/adminsdk
  // Add the contents of it to the environment variable MAGIKARP_JSON with a command like
  // export MAGIKARP_JSON=$(cat magikarp.json)
  public static final String FIREBASE_SERVICE_ACCOUNT_ENV_VAR = "MAGIKARP_JSON";
  public static final String FIRESTORE_URL = "https://magikarp-295201.firebaseio.com";

  public static final String COLLECTION_PATH = "messages";

  public static final String PROJECT_ID = "magikarp-295201";
  public static final String GCS_BUCKET_ROOT = "https://storage.googleapis.com/";
  public static final String PROJECT_BUCKET = "magikarp-images";
  public static final String FULL_PROJECT_BUCKET = GCS_BUCKET_ROOT + PROJECT_BUCKET;

}
