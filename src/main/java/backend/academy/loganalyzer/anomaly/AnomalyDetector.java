package backend.academy.loganalyzer.anomaly;

import java.util.List;

public interface AnomalyDetector {
    List<Anomaly> detect(List<MetricSnapshot> history);
}
