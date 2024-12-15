package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class LogFilterTest {

    @Test
    void filter_ShouldReturnFilteredRecords_WhenConditionIsMet() {
        // Arrange
        List<LogRecord> records = List.of(
            createLogRecord("127.0.0.1", "INFO", "Test log 1"),
            createLogRecord("127.0.0.2", "ERROR", "Test log 2"),
            createLogRecord("127.0.0.3", "INFO", "Test log 3")
        );

        LogFilter filter = recs -> recs.stream()
            .filter(r -> "ERROR".equals(r.method()))
            .collect(Collectors.toList());

        // Act
        List<LogRecord> result = filter.filter(records);

        // Assert
        assertEquals(1, result.size());
        assertEquals("127.0.0.2", result.getFirst().remoteAddr());
        assertEquals("Test log 2", result.getFirst().request());
    }

    @Test
    void filter_ShouldReturnEmptyList_WhenNoRecordsMatch() {
        // Arrange
        List<LogRecord> records = List.of(
            createLogRecord("127.0.0.1", "INFO", "Test log 1"),
            createLogRecord("127.0.0.2", "INFO", "Test log 2")
        );

        LogFilter filter = recs -> recs.stream()
            .filter(r -> "ERROR".equals(r.method()))
            .collect(Collectors.toList());

        // Act
        List<LogRecord> result = filter.filter(records);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void filter_ShouldHandleEmptyList() {
        // Arrange
        List<LogRecord> records = List.of();

        LogFilter filter = recs -> recs.stream()
            .filter(r -> "ERROR".equals(r.method()))
            .collect(Collectors.toList());

        // Act
        List<LogRecord> result = filter.filter(records);

        // Assert
        assertTrue(result.isEmpty());
    }

    private LogRecord createLogRecord(String remoteAddr, String method, String request) {
        LogRecord record = new LogRecord();
        record.remoteAddr(remoteAddr);
        record.method(method);
        record.request(request);
        record.timestamp(LocalDateTime.now().minusDays(1));
        return record;
    }
}
