package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.HttpMethod;
import io.github.therad445.loganalyzer.model.LogRecord;
import io.github.therad445.loganalyzer.model.StatusClass;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldLogFilterTest {

    private List<LogRecord> logs;

    @BeforeEach
    void setUp() {
        logs = List.of(
            new LogRecord("127.0.0.1", "admin", LocalDateTime.now(), HttpMethod.GET, "/index.html", 200, 512L, "-",
                "curl/7.81"),
            new LogRecord("192.168.0.1", "-", LocalDateTime.now(), HttpMethod.POST, "/api/data", 404, 1024L, "-",
                "PostmanRuntime"),
            new LogRecord("10.0.0.1", "guest", LocalDateTime.now(), HttpMethod.GET, "/status", 500, 2048L, "-",
                "Mozilla/5.0")
        );
    }

    @Test
    void filterByMethod_shouldReturnMatching() {
        FieldLogFilter filter = new FieldLogFilter("method", "GET");
        List<LogRecord> result = filter.filter(logs);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.method() == HttpMethod.GET));
    }

    @Test
    void filterByStatus_shouldMatchStatusClass() {
        FieldLogFilter filter = new FieldLogFilter("status", "404");
        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertEquals(StatusClass.CLIENT_ERROR, StatusClass.fromStatusCode(result.get(0).status()));
    }

    @Test
    void filterByAgent_shouldMatchPrefixIgnoringWildcard() {
        FieldLogFilter filter = new FieldLogFilter("agent", "curl*");
        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertTrue(result.get(0).userAgent().startsWith("curl"));
    }

    @Test
    void filterByRequest_shouldContainSubstring() {
        FieldLogFilter filter = new FieldLogFilter("request", "/api");
        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertTrue(result.get(0).request().contains("/api"));
    }

    @Test
    void filterByRemoteAddr_shouldMatchExactIp() {
        FieldLogFilter filter = new FieldLogFilter("remoteAddr", "10.0.0.1");
        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertEquals("10.0.0.1", result.get(0).remoteAddr());
    }

    @Test
    void unknownField_shouldReturnEmpty() {
        FieldLogFilter filter = new FieldLogFilter("nonexistent", "xxx");
        List<LogRecord> result = filter.filter(logs);

        assertTrue(result.isEmpty());
    }
}
