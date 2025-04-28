package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.LogRecord;
import java.util.List;
import java.util.stream.Collectors;

public class FieldLogFilter implements LogFilter {
    private final String fieldName;
    private final String fieldValue;

    public FieldLogFilter(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public List<LogRecord> filter(List<LogRecord> logs) {
        return logs.stream()
            .filter(log -> switch (fieldName) {
                case "method" -> log.method().equalsIgnoreCase(fieldValue);
                case "agent" -> log.userAgent().startsWith(fieldValue.replace("*", ""));
                case "status" -> String.valueOf(log.status()).equals(fieldValue);
                case "request" -> log.request().contains(fieldValue);
                case "remoteAddr" -> log.remoteAddr().equals(fieldValue);
                default -> false;
            })
            .collect(Collectors.toList());
    }

}
