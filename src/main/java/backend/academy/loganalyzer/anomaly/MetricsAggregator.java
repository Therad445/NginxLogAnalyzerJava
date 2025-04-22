package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricsAggregator {
    private final Duration window;

    public MetricsAggregator(Duration window) {
        this.window = window == null ? Duration.ofMinutes(1) : window;
    }

    public List<MetricSnapshot> aggregate(List<LogRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        return records.stream()
            .collect(Collectors.groupingBy(this::truncate))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> toSnapshot(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    private Instant truncate(LogRecord rec) {
        LocalDateTime ldt = rec.timestamp();
        if (ldt == null) {
            ldt = LocalDateTime.now();
        }
        long epoch = ldt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long truncated = epoch - (epoch % window.getSeconds());
        return Instant.ofEpochSecond(truncated);
    }

    private MetricSnapshot toSnapshot(Instant windowEnd, List<LogRecord> list) {
        long requests = list.size();
        long errors = list.stream().filter(r -> r.status() >= 400).count();
        double meanLatency = 0.0;
        return new MetricSnapshot(windowEnd, requests, errors, meanLatency);
    }
}
