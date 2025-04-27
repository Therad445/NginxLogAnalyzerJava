package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class URLReaderTest {

    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void readFromUrlWithUtilityMethod() throws IOException {
        String body = "foo\nbar\nbaz";
        server.enqueue(new MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(body));

        String url = server.url("/test").toString();
        URLReader ur = new URLReader(url);

        List<String> lines = assertTimeoutPreemptively(
            java.time.Duration.ofSeconds(5),
            () -> {
                try (Stream<String> stream = ur.read()) {
                    return stream.toList();
                }
            }
        );
        assertThat(lines).containsExactly("foo", "bar", "baz");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void readFromUrlWithConstructor() throws IOException {
        String body = "x\ny";
        server.enqueue(new MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(body));

        String url = server.url("/test2").toString();
        URLReader ur = new URLReader(url);

        List<String> lines = assertTimeoutPreemptively(
            java.time.Duration.ofSeconds(5),
            () -> {
                try (Stream<String> stream = ur.read()) {
                    return stream.toList();
                }
            }
        );
        assertThat(lines).containsExactly("x", "y");
    }

    @Test
    void readInvalidUrlThrowsIOException() {
        String url = "http://127.0.0.1:54321/";
        URLReader ur = new URLReader(url);
        assertThatThrownBy(() -> ur.read().count())
            .isInstanceOf(IOException.class);
    }

}
