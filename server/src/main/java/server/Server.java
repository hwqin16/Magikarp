package server;

import io.javalin.Javalin;

public class Server {
    // TODO use this to build GoogleIdTokenVerifier
    private static final String OAUTH_CLIENT_ID =
            "222054183884-l3lcjqflihkqibfu4s3cu1lk1vhblv5l.apps.googleusercontent.com";

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> ctx.result("Hello World"));
    }
}