package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LogAnalyzerTest {

    private final LogAnalyzer logAnalyzer = new LogAnalyzer();

    @Test
    public void testCountResources() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(200);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        LogRecord log3 = new LogRecord();
        log3.request("/home");
        log3.remoteAddr("192.168.1.1");
        log3.remoteUser("user1");
        log3.timeLocal(LocalDateTime.now());
        log3.method("GET");
        log3.status(200);
        log3.bodyBytesSent(1500);
        log3.httpReferer("");
        log3.userAgent("");
        log3.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2, log3);

        // Act
        Map<String, Long> result = logAnalyzer.countResources(logs);

        // Assert
        assertEquals(2, result.size());
        assertEquals(2, result.get("/home"));
        assertEquals(1, result.get("/login"));
    }

    @Test
    public void testCountStatusCodes() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(404);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2, log1);

        // Act
        Map<Integer, Long> result = logAnalyzer.countStatusCodes(logs);

        // Assert
        assertEquals(2, result.size());
        assertEquals(2, result.get(200));
        assertEquals(1, result.get(404));
    }

    @Test
    public void testAverageResponseSize() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(200);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);

        // Act
        double result = logAnalyzer.averageResponseSize(logs);

        // Assert
        assertEquals(1250.0, result, 0.1);
    }

    @Test
    public void testCountTotalRequests() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(200);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);

        // Act
        long result = logAnalyzer.countTotalRequests(logs);

        // Assert
        assertEquals(2, result);
    }

    @Test
    public void testApplyFilter() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(200);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2);

        LogFilter filter = logs1 -> logs1.stream()
            .filter(log -> log.status() == 200)
            .toList();

        // Act
        List<LogRecord> result = logAnalyzer.applyFilter(logs, filter);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    public void testPercentile95ResponseSize() {
        // Arrange
        LogRecord log1 = new LogRecord();
        log1.request("/home");
        log1.remoteAddr("192.168.1.1");
        log1.remoteUser("user1");
        log1.timeLocal(LocalDateTime.now());
        log1.method("GET");
        log1.status(200);
        log1.bodyBytesSent(1500);
        log1.httpReferer("");
        log1.userAgent("");
        log1.timestamp(LocalDateTime.now());

        LogRecord log2 = new LogRecord();
        log2.request("/login");
        log2.remoteAddr("192.168.1.2");
        log2.remoteUser("user2");
        log2.timeLocal(LocalDateTime.now());
        log2.method("POST");
        log2.status(200);
        log2.bodyBytesSent(1000);
        log2.httpReferer("");
        log2.userAgent("");
        log2.timestamp(LocalDateTime.now());

        LogRecord log3 = new LogRecord();
        log3.request("/home");
        log3.remoteAddr("192.168.1.3");
        log3.remoteUser("user3");
        log3.timeLocal(LocalDateTime.now());
        log3.method("GET");
        log3.status(200);
        log3.bodyBytesSent(2000);
        log3.httpReferer("");
        log3.userAgent("");
        log3.timestamp(LocalDateTime.now());

        List<LogRecord> logs = Arrays.asList(log1, log2, log3);

        // Act
        double result = logAnalyzer.percentile95ResponseSize(logs);

        // Assert
        assertEquals(2000.0, result, 0.1);
    }

    @Test
    public void testPercentile95WithEmptyList() {
        // Arrange
        List<LogRecord> logs = Collections.emptyList();
        LogAnalyzer analyzer = new LogAnalyzer();

        // Act
        double result = analyzer.percentile95ResponseSize(logs);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    public void testPercentile95WithOneElement() {
        // Arrange
        LogRecord log = new LogRecord();
        log.bodyBytesSent(500L);
        List<LogRecord> logs = List.of(log);
        LogAnalyzer analyzer = new LogAnalyzer();

        // Act
        double result = analyzer.percentile95ResponseSize(logs);

        // Assert
        assertEquals(500.0, result);
    }
}
