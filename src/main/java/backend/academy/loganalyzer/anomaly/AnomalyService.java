package backend.academy.loganalyzer.anomaly;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnomalyService {
    private final List<AnomalyDetector> detectors;

    public AnomalyService(List<AnomalyDetector> detectors) {
        this.detectors = detectors;
    }

    public Map<String, List<Anomaly>> detectAll(List<MetricSnapshot> snapshots) {
        return detectors.stream().flatMap(d -> d.detect(snapshots).stream())
            .collect(Collectors.groupingBy(Anomaly::metric));
    }
}
