package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import java.util.List;

@FunctionalInterface
public interface LogFilter {
    List<LogRecord> filter(List<LogRecord> records);
}
