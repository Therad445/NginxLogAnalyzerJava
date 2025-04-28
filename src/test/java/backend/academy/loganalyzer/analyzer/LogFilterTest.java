package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.HttpMethod;
import backend.academy.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogFilterTest {

    @Test
    void filter_ShouldReturnFilteredRecords_WhenConditionIsMet() {
        List<LogRecord> records = List.of(
            createLogRecord("127.0.0.1", HttpMethod.GET, "Test log 1"),
            createLogRecord("127.0.0.2", HttpMethod.POST, "Test log 2"),
            createLogRecord("127.0.0.3", HttpMethod.GET, "Test log 3")
        );

        LogFilter filter = recs -> recs.stream()
            .filter(r -> r.method() == HttpMethod.POST)
            .collect(Collectors.toList());

        List<LogRecord> result = filter.filter(records);

        assertEquals(1, result.size());
        assertEquals("127.0.0.2", result.getFirst().remoteAddr());
        assertEquals("Test log 2", result.getFirst().request());
    }

    @Test
    void filter_ShouldReturnEmptyList_WhenNoRecordsMatch() {
        List<LogRecord> records = List.of(
            createLogRecord("127.0.0.1", HttpMethod.GET, "Test log 1"),
            createLogRecord("127.0.0.2", HttpMethod.GET, "Test log 2")
        );

        LogFilter filter = recs -> recs.stream()
            .filter(r -> r.method() == HttpMethod.POST)
            .collect(Collectors.toList());

        List<LogRecord> result = filter.filter(records);

        assertTrue(result.isEmpty());
    }

    @Test
    void filter_ShouldHandleEmptyList() {
        List<LogRecord> records = List.of();

        LogFilter filter = recs -> recs.stream()
            .filter(r -> r.method() == HttpMethod.POST)
            .collect(Collectors.toList());

        List<LogRecord> result = filter.filter(records);

        assertTrue(result.isEmpty());
    }

    private LogRecord createLogRecord(String remoteAddr, HttpMethod method, String request) {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        return new LogRecord(
            remoteAddr,
            null,
            now,
            method,
            request,
            200,
            500L,
            null,
            "Mozilla"
        );
    }
}
