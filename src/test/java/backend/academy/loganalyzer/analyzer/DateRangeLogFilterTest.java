package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeLogFilterTest {

    @Test
    public void testFilterWithinRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = new LogRecord();
        log1.timestamp(LocalDateTime.of(2024, 12, 5, 10, 30));

        LogRecord log2 = new LogRecord();
        log2.timestamp(LocalDateTime.of(2024, 12, 8, 15, 45));

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
    }

    @Test
    public void testFilterOutsideRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = new LogRecord();
        log1.timestamp(LocalDateTime.of(2024, 11, 30, 23, 59));

        LogRecord log2 = new LogRecord();
        log2.timestamp(LocalDateTime.of(2024, 12, 11, 0, 0));

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterOnBoundary() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        LogRecord log1 = new LogRecord();
        log1.timestamp(start);

        LogRecord log2 = new LogRecord();
        log2.timestamp(end);

        List<LogRecord> logs = Arrays.asList(log1, log2);
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
    }

    @Test
    public void testFilterWithEmptyList() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 10, 23, 59);

        List<LogRecord> logs = Collections.emptyList();
        DateRangeLogFilter filter = new DateRangeLogFilter(start, end);

        // Act
        List<LogRecord> result = filter.filter(logs);

        // Assert
        assertTrue(result.isEmpty());
    }
}
