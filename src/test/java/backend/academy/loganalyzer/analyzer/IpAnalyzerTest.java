package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.HttpMethod;
import backend.academy.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpAnalyzerTest {

    private List<LogRecord> logs;
    private IpAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new IpAnalyzer();
        logs = List.of(
            new LogRecord("192.168.0.1", "-", LocalDateTime.now(), HttpMethod.GET, "/index", 200, 1000, "-", "curl"),
            new LogRecord("192.168.0.1", "-", LocalDateTime.now(), HttpMethod.POST, "/upload", 404, 500, "-", "curl"),
            new LogRecord("10.0.0.1", "-", LocalDateTime.now(), HttpMethod.GET, "/status", 500, 300, "-", "curl"),
            new LogRecord("10.0.0.2", "-", LocalDateTime.now(), HttpMethod.GET, "/info", 200, 250, "-", "curl")
        );
    }

    @Test
    void countRequestsPerIp_shouldReturnCorrectCounts() {
        Map<String, Long> result = analyzer.countRequestsPerIp(logs);

        assertEquals(3, result.size());
        assertEquals(2L, result.get("192.168.0.1"));
        assertEquals(1L, result.get("10.0.0.1"));
        assertEquals(1L, result.get("10.0.0.2"));
    }

    @Test
    void countErrorsPerIp_shouldReturnOnly400Plus() {
        Map<String, Long> result = analyzer.countErrorsPerIp(logs);

        assertEquals(2, result.size());
        assertEquals(1L, result.get("192.168.0.1"));
        assertEquals(1L, result.get("10.0.0.1"));
        assertNull(result.get("10.0.0.2")); // нет ошибок
    }

    @Test
    void emptyLogs_shouldReturnEmptyMaps() {
        List<LogRecord> empty = List.of();

        assertTrue(analyzer.countRequestsPerIp(empty).isEmpty());
        assertTrue(analyzer.countErrorsPerIp(empty).isEmpty());
    }
}
