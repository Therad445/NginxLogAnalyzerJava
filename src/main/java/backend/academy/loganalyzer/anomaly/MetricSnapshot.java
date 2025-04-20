package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import java.util.Objects;

public final class MetricSnapshot {
    private final Instant timestamp;
    private final long requests;
    private final long errors;
    private final double meanLatencyMillis;

    public MetricSnapshot(Instant timestamp, long requests, long errors, double meanLatencyMillis) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.requests = requests;
        this.errors = errors;
        this.meanLatencyMillis = meanLatencyMillis;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public long requests() {
        return requests;
    }

    public long errors() {
        return errors;
    }

    public double meanLatencyMillis() {
        return meanLatencyMillis;
    }
}




