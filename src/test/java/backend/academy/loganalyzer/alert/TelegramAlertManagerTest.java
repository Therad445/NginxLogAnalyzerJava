package backend.academy.loganalyzer.alert;

import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramAlertManagerTest {

    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    void sendsProperlyFormattedMessage() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ok\":true}"));

        String fakeToken = "123456:test";
        String fakeChatId = "987654321";
        String fakeText = "Hello *world*!";

        TelegramAlertManager manager = new TelegramAlertManager(fakeToken, fakeChatId) {
            @Override
            public void send(String text) {
                try {
                    RequestBody body = new FormBody.Builder()
                        .add("chat_id", fakeChatId)
                        .add("text", text)
                        .add("parse_mode", "MarkdownV2")
                        .build();

                    Request req = new Request.Builder()
                        .url(server.url("/bot" + fakeToken + "/sendMessage"))
                        .post(body)
                        .build();

                    client.newCall(req).execute().close();
                } catch (IOException ignored) {
                }
            }
        };

        manager.send(fakeText);

        var recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertTrue(recorded.getBody().readUtf8().contains("text=Hello%20*world*%21"));
        assertTrue(recorded.getPath().contains("sendMessage"));
    }
}
