package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.HttpMethod;
import backend.academy.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static backend.academy.loganalyzer.model.HttpMethod.GET;
import static backend.academy.loganalyzer.model.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LogAnalyzerTest {

    private final LogAnalyzer logAnalyzer = new LogAnalyzer();

    @Test
    public void testCountResources() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 200, 1000L);
        LogRecord log3 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);

        List<LogRecord> logs = Arrays.asList(log1, log2, log3);

        Map<String, Long> result = logAnalyzer.countResources(logs);

        assertEquals(2, result.size());
        assertEquals(2, result.get("/home"));
        assertEquals(1, result.get("/login"));
    }

    @Test
    public void testCountStatusCodes() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 404, 1000L);

        List<LogRecord> logs = Arrays.asList(log1, log2, log1);

        Map<Integer, Long> result = logAnalyzer.countStatusCodes(logs);

        assertEquals(2, result.size());
        assertEquals(2, result.get(200));
        assertEquals(1, result.get(404));
    }

    @Test
    public void testAverageResponseSize() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 200, 1000L);

        List<LogRecord> logs = Arrays.asList(log1, log2);

        double result = logAnalyzer.averageResponseSize(logs);

        assertEquals(1250.0, result, 0.1);
    }

    @Test
    public void testCountTotalRequests() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 200, 1000L);

        List<LogRecord> logs = Arrays.asList(log1, log2);

        long result = logAnalyzer.countTotalRequests(logs);

        assertEquals(2, result);
    }

    @Test
    public void testApplyFilter() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 200, 1000L);

        List<LogRecord> logs = Arrays.asList(log1, log2);

        LogFilter filter = logs1 -> logs1.stream()
            .filter(log -> log.status() == 200)
            .toList();

        List<LogRecord> result = logAnalyzer.applyFilter(logs, filter);

        assertEquals(2, result.size());
    }

    @Test
    public void testPercentile95ResponseSize() {
        LogRecord log1 = createLogRecord("192.168.1.1", "user1", GET, "/home", 200, 1500L);
        LogRecord log2 = createLogRecord("192.168.1.2", "user2", POST, "/login", 200, 1000L);
        LogRecord log3 = createLogRecord("192.168.1.3", "user3", GET, "/home", 200, 2000L);

        List<LogRecord> logs = Arrays.asList(log1, log2, log3);

        double result = logAnalyzer.percentile95ResponseSize(logs);

        assertEquals(2000.0, result, 0.1);
    }

    @Test
    public void testPercentile95WithEmptyList() {
        List<LogRecord> logs = Collections.emptyList();

        double result = logAnalyzer.percentile95ResponseSize(logs);

        assertEquals(0.0, result);
    }

    @Test
    public void testPercentile95WithOneElement() {
        LogRecord log = createLogRecord("192.168.1.1", "user1", GET, "/single", 200, 500L);

        List<LogRecord> logs = List.of(log);

        double result = logAnalyzer.percentile95ResponseSize(logs);

        assertEquals(500.0, result);
    }

    private LogRecord createLogRecord(
        String remoteAddr,
        String remoteUser,
        HttpMethod method,
        String request,
        int status,
        long bodyBytesSent
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new LogRecord(
            remoteAddr,
            remoteUser,
            now,
            method,
            request,
            status,
            bodyBytesSent,
            "-",
            "Mozilla"
        );
    }
}
