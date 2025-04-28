package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldLogFilterTest {

    @Test
    public void testFilterByMethod() {
        LogRecord log1 = createLogRecord("GET", "/home", 200, "Mozilla/5.0");
        LogRecord log2 = createLogRecord("POST", "/login", 200, "Mozilla/5.0");

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("method", "GET");

        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertEquals("GET", result.getFirst().method());
    }

    @Test
    public void testFilterByAgent() {
        LogRecord log1 = createLogRecord("GET", "/home", 200, "Mozilla/5.0");
        LogRecord log2 = createLogRecord("POST", "/login", 200, "Chrome/90.0");

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("agent", "Mozilla");

        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().userAgent().startsWith("Mozilla"));
    }

    @Test
    public void testFilterByStatus() {
        LogRecord log1 = createLogRecord("GET", "/home", 200, "Mozilla");
        LogRecord log2 = createLogRecord("POST", "/login", 404, "Mozilla");

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("status", "200");

        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertEquals(200, result.getFirst().status());
    }

    @Test
    public void testFilterByRequest() {
        LogRecord log1 = createLogRecord("GET", "/home", 200, "Mozilla");
        LogRecord log2 = createLogRecord("POST", "/login", 200, "Mozilla");

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("request", "home");

        List<LogRecord> result = filter.filter(logs);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().request().contains("home"));
    }

    @Test
    public void testFilterWithInvalidField() {
        LogRecord log1 = createLogRecord("GET", "/home", 200, "Mozilla");

        List<LogRecord> logs = Collections.singletonList(log1);
        FieldLogFilter filter = new FieldLogFilter("invalidField", "value");

        List<LogRecord> result = filter.filter(logs);

        assertTrue(result.isEmpty());
    }

    private LogRecord createLogRecord(String method, String request, int status, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        return new LogRecord(
            "127.0.0.1",
            "-",
            now,
            method,
            request,
            status,
            1000L,
            "-",
            userAgent,
            now
        );
    }
}
