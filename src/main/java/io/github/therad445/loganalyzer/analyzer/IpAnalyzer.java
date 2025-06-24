package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpAnalyzer {

    public Map<String, Long> countRequestsPerIp(List<LogRecord> logs) {
        Map<String, Long> counts = new HashMap<>();
        for (LogRecord r : logs) {
            counts.merge(r.remoteAddr(), 1L, Long::sum);
        }
        return counts;
    }

    public Map<String, Long> countErrorsPerIp(List<LogRecord> logs) {
        Map<String, Long> counts = new HashMap<>();
        for (LogRecord r : logs) {
            if (r.status() >= 400) {
                counts.merge(r.remoteAddr(), 1L, Long::sum);
            }
        }
        return counts;
    }
}
