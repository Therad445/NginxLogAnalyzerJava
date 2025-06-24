package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.util.List;

@FunctionalInterface
public interface LogFilter {
    List<LogRecord> filter(List<LogRecord> records);
}
