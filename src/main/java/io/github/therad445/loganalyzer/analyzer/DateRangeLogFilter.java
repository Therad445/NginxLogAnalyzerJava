package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DateRangeLogFilter implements LogFilter {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public DateRangeLogFilter(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public List<LogRecord> filter(List<LogRecord> logs) {
        return logs.stream()
            .filter(log -> !log.timestamp().isBefore(start) && !log.timestamp().isAfter(end))
            .collect(Collectors.toList());
    }
}
