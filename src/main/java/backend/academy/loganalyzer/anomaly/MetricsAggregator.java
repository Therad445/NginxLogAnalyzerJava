package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class MetricsAggregator {
    private final Duration window;
    private final int chartEveryN;

    private final List<LogRecord> buffer = new ArrayList<>();
    private final List<MetricSnapshot> history = new CopyOnWriteArrayList<>();
    private Instant currentWindowEnd = null;
    private int windowsSinceChart = 0;

    public MetricsAggregator(Duration window, int chartEveryN) {
        this.window = window != null ? window : Duration.ofMinutes(1);
        this.chartEveryN = chartEveryN > 0 ? chartEveryN : 5;
    }

    public List<MetricSnapshot> aggregate(List<LogRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        return records.stream()
            .collect(Collectors.groupingBy(this::truncateToWindow))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> toSnapshot(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    public synchronized List<MetricSnapshot> addAndAggregate(LogRecord rec) {
        Instant win = truncateToWindow(rec);
        if (currentWindowEnd == null) {
            currentWindowEnd = win;
        }
        if (win.equals(currentWindowEnd)) {
            buffer.add(rec);
        } else {
            history.add(toSnapshot(currentWindowEnd, buffer));
            windowsSinceChart++;
            buffer.clear();
            currentWindowEnd = win;
            buffer.add(rec);
        }
        return List.copyOf(history);
    }

    public synchronized boolean shouldEmitChart() {
        if (windowsSinceChart >= chartEveryN) {
            windowsSinceChart = 0;
            return true;
        }
        return false;
    }

    private Instant truncateToWindow(LogRecord rec) {
        LocalDateTime ldt = Optional.ofNullable(rec.timestamp()).orElse(LocalDateTime.now());
        long epoch = ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
        long secs = window.getSeconds();
        long end = epoch - (epoch % secs) + secs;
        return Instant.ofEpochSecond(end);
    }

    private MetricSnapshot toSnapshot(Instant windowEnd, List<LogRecord> list) {
        long requests = list.size();
        long errors = list.stream().filter(r -> r.status() >= 400).count();
        double meanLatency = 0.0;
        double errorRate = requests == 0 ? 0.0 : (double) errors / requests;
        return new MetricSnapshot(windowEnd, requests, errors, meanLatency, errorRate, requests);
    }
}
