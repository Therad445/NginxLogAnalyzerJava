package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import java.util.Objects;

public record MetricSnapshot(Instant timestamp, long requests, long errors, double meanLatencyMillis) {
    public MetricSnapshot(Instant timestamp, long requests, long errors, double meanLatencyMillis) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.requests = requests;
        this.errors = errors;
        this.meanLatencyMillis = meanLatencyMillis;
    }
}




