package io.github.therad445.loganalyzer.model;

import io.github.therad445.loganalyzer.anomaly.Anomaly;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record LogResult(
    long totalRequests,
    double averageResponseSize,
    Map<String, Long> resourceCounts,
    Map<Integer, Long> statusCodeCounts,
    double percentile,
    Map<String, List<Anomaly>> anomalies,
    Set<String> suspiciousIps) {
    public LogResult {
        if (totalRequests < 0) {
            throw new IllegalArgumentException("totalRequests меньше нуля");
        }
        if (averageResponseSize < 0) {
            throw new IllegalArgumentException("averageResponseSize меньше нуля");
        }
        if (resourceCounts == null) {
            throw new NullPointerException("resourceCounts пустой");
        }
        if (statusCodeCounts == null) {
            throw new NullPointerException("statusCodeCounts пустой");
        }
        if (percentile < 0) {
            throw new IllegalArgumentException("percentile меньше нуля");
        }
        if (anomalies == null) {
            anomalies = Map.of();
        }
    }
}
