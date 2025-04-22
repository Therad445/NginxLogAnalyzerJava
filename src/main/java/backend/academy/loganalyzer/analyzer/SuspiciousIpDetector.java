package backend.academy.loganalyzer.analyzer;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

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
