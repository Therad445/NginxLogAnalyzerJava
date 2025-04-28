package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.model.HttpMethod;
import backend.academy.loganalyzer.model.LogRecord;
import backend.academy.loganalyzer.model.StatusClass;
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
                case "method" -> log.method() == HttpMethod.fromString(fieldValue);
                case "status" -> StatusClass.fromStatusCode(log.status())
                    == StatusClass.fromStatusCode(Integer.parseInt(fieldValue));
                case "agent" -> log.userAgent().startsWith(fieldValue.replace("*", ""));
                case "request" -> log.request().contains(fieldValue);
                case "remoteAddr" -> log.remoteAddr().equals(fieldValue);
                default -> false;
            })
            .collect(Collectors.toList());
    }


}
