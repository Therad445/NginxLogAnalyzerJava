package io.github.therad445.loganalyzer.anomaly;

import java.util.List;

public interface AnomalyDetector {
    List<Anomaly> detect(List<MetricSnapshot> history);
}
