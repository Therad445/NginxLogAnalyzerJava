package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
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
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.method("GET");
        log1.request("/home");
        log1.status(200);
        log1.userAgent("Mozilla");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.method("POST");
        log2.request("/login");
        log2.status(200);
        log2.userAgent("Mozilla");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("method", "GET");

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(1, result.size());
        assertEquals("GET", result.getFirst().method());
    }

    @Test
    public void testFilterByAgent() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.method("GET");
        log1.request("/home");
        log1.status(200);
        log1.userAgent("Mozilla/5.0");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.method("POST");
        log2.request("/login");
        log2.status(200);
        log2.userAgent("Chrome/90.0");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("agent", "Mozilla");

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.getFirst().userAgent().startsWith("Mozilla"));
    }

    @Test
    public void testFilterByStatus() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.method("GET");
        log1.request("/home");
        log1.status(200);
        log1.userAgent("Mozilla");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.method("POST");
        log2.request("/login");
        log2.status(404);
        log2.userAgent("Mozilla");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("status", "200");

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(1, result.size());
        assertEquals(200, result.getFirst().status());
    }

    @Test
    public void testFilterByRequest() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.method("GET");
        log1.request("/home");
        log1.status(200);
        log1.userAgent("Mozilla");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.method("POST");
        log2.request("/login");
        log2.status(200);
        log2.userAgent("Mozilla");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);
        FieldLogFilter filter = new FieldLogFilter("request", "home");

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.getFirst().request().contains("home"));
    }

    @Test
    public void testFilterWithInvalidField() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.method("GET");
        log1.request("/home");
        log1.status(200);
        log1.userAgent("Mozilla");
        log1.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Collections.singletonList(log1);
        FieldLogFilter filter = new FieldLogFilter("invalidField", "value");

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertTrue(result.isEmpty());
    }
}
