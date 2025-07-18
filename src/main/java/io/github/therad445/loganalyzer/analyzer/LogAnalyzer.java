package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogAnalyzer {

    public static final double PERCENTILE = 0.95;

    public Map<String, Long> countResources(List<LogRecord> logs) {
        return logs.stream()
            .collect(Collectors.groupingBy(LogRecord::request, Collectors.counting()));
    }

    public Map<Integer, Long> countStatusCodes(List<LogRecord> logs) {
        return logs.stream()
            .collect(Collectors.groupingBy(LogRecord::status, Collectors.counting()));
    }

    public double averageResponseSize(List<LogRecord> logs) {
        return logs.stream()
            .mapToLong(LogRecord::bodyBytesSent)
            .average()
            .orElse(0.0);

    }

    public long countTotalRequests(List<LogRecord> logs) {
        return logs.size();
    }

    public List<LogRecord> applyFilter(List<LogRecord> logs, LogFilter filter) {
        return filter.filter(logs);
    }

    public double percentile95ResponseSize(List<LogRecord> logs) {
        List<Long> sortedSizes = logs.stream()
            .map(LogRecord::bodyBytesSent)
            .sorted()
            .toList();

        if (sortedSizes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(PERCENTILE * sortedSizes.size()) - 1;
        return sortedSizes.get(index);
    }
}
