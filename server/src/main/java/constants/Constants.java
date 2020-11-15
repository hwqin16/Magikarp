package constants;

public class Constants {
    // TODO use this to build GoogleIdTokenVerifier
    public static final String OAUTH_CLIENT_ID =
            "222054183884-l3lcjqflihkqibfu4s3cu1lk1vhblv5l.apps.googleusercontent.com";
    public static final int PORT = 7000;
    // Everyone will need to create a file like below from
    // https://console.firebase.google.com/u/1/project/magikarp-295201/settings/serviceaccounts/adminsdk
    // rename to magikarp.json and stick it in /tmp
    public static final String FIREBASE_SERVICE_ACCOUNT_FILE = "/tmp/magikarp.json";
    public static final String FIRESTORE_URL = "https://magikarp-295201.firebaseio.com";

    public static final String COLLECTION_PATH = "messages";

    public static final String PROJECT_ID = "magikarp-295201";
    public static final String PROJECT_BUCKET = "magikarp-images";

}
