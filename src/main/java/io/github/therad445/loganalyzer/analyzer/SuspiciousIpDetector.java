package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.LogRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SuspiciousIpDetector {

    private final Duration window;
    private final int requestThreshold;

    public SuspiciousIpDetector(Duration window, int requestThreshold) {
        this.window = window;
        this.requestThreshold = requestThreshold;
    }

    public Set<String> detect(List<LogRecord> logs) {
        Map<Instant, Map<String, Integer>> ipCountsPerWindow = new HashMap<>();

        for (LogRecord r : logs) {
            Instant timestamp = r.timestamp().atZone(ZoneId.systemDefault()).toInstant();
            Instant windowKey = truncateToWindow(timestamp);
            ipCountsPerWindow
                .computeIfAbsent(windowKey, k -> new HashMap<>())
                .merge(r.remoteAddr(), 1, Integer::sum);
        }

        Set<String> suspiciousIps = new HashSet<>();
        for (Map<String, Integer> ipCounts : ipCountsPerWindow.values()) {
            for (Map.Entry<String, Integer> entry : ipCounts.entrySet()) {
                if (entry.getValue() >= requestThreshold) {
                    suspiciousIps.add(entry.getKey());
                }
            }
        }

        return suspiciousIps;
    }

    private Instant truncateToWindow(Instant timestamp) {
        long seconds = timestamp.getEpochSecond();
        long truncated = seconds - (seconds % window.getSeconds());
        return Instant.ofEpochSecond(truncated);
    }
}
