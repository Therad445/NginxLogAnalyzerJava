package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static io.github.therad445.loganalyzer.model.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeLogFilterTest {

    @Test
    public void testFilterWithinRange() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = createLogRecordWithTimestamp(LocalDateTime.of(2024, 12, 5, 10, 30));
        LogRecord log2 = createLogRecordWithTimestamp(LocalDateTime.of(2024, 12, 8, 15, 45));

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        List<LogRecord> result = filter.filter(logs);

        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
    }

    @Test
    public void testFilterOutsideRange() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = createLogRecordWithTimestamp(LocalDateTime.of(2024, 11, 30, 23, 59));
        LogRecord log2 = createLogRecordWithTimestamp(LocalDateTime.of(2024, 12, 11, 0, 0));

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        List<LogRecord> result = filter.filter(logs);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterOnBoundary() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = createLogRecordWithTimestamp(start);
        LogRecord log2 = createLogRecordWithTimestamp(end);

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        List<LogRecord> result = filter.filter(logs);

        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
    }

    @Test
    public void testFilterWithEmptyList() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        List<LogRecord> logs = Collections.emptyList();
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        List<LogRecord> result = filter.filter(logs);

        assertTrue(result.isEmpty());
    }

    private LogRecord createLogRecordWithTimestamp(LocalDateTime timestamp) {
        return new LogRecord(
            "127.0.0.1",
            "-",
            timestamp,
            GET,
            "/",
            200,
            1000L,
            "-",
            "Mozilla"
        );
    }
}
