package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LogAnalyzer.java — отвечает за сбор и вычисление статистики на основе парсированных логов.
 */
public class LogAnalyzer {

    public long countTotalRequests(List<LogRecord> logs) {
        return logs.size();
    }

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

    public List<LogRecord> filterLogs(List<LogRecord> logs, String filterField, String filterValue) {
        return logs.stream()
            .filter(log -> {
                return switch (filterField) {
                    case "method" -> log.method().equalsIgnoreCase(filterValue);
                    case "agent" -> log.userAgent().startsWith(filterValue.replace("*", ""));
                    case "status" -> String.valueOf(log.status()).equals(filterValue);
                    case "request" -> log.request().contains(filterValue);
                    default -> false;
                };
            })
            .collect(Collectors.toList());
    }

}
