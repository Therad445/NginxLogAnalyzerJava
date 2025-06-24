package io.github.therad445.loganalyzer.anomaly;

import java.time.Instant;

public record Anomaly(Instant timestamp, String metric, double value, double threshold, double score) {
}
