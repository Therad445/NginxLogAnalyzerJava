package io.github.therad445.loganalyzer.anomaly;

import java.time.Instant;
import java.util.Objects;

public record MetricSnapshot(
    Instant timestamp,
    long requests,
    long errors,
    double meanLatencyMillis,
    double errorRate,
    long reqsPerWindow
) {
    public MetricSnapshot {
        Objects.requireNonNull(timestamp);
        if (requests == 0) {
            errorRate = 0.0;
        }
    }
}
