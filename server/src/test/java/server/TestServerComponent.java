package server;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Right now this runs against our production database on real data. So the tests will start failing
 * once actual data is passing through it. We should use an emulated database or a staging database or something.
 * At minimum, we can create and then delete data in Antarctica or something for tests.
 * But, right now, when we don't have any of the write paths, we'll just use the production db.
 */
public class TestServerComponent {
    @BeforeAll
    public static void setup() {
        Server.start();
    }

    @Test
    public void TestUserMessagesEmpty() {

    }

    @AfterAll
    public static void stop() {
        Server.stop();
    }
}
