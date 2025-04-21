package backend.academy.loganalyzer.anomaly;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyConfiguratorTest {

    @Test
    void defaultServiceReturnsConfiguredDetectors() {
        AnomalyService service = AnomalyConfigurator.defaultService();

        List<MetricSnapshot> fakeSnapshots = List.of(
            new MetricSnapshot(Instant.parse("2024-01-01T00:00:00Z"), 10, 1, 100.0),
            new MetricSnapshot(Instant.parse("2024-01-01T00:01:00Z"), 10, 1, 100.0),
            new MetricSnapshot(Instant.parse("2024-01-01T00:02:00Z"), 10, 1, 100.0),
            new MetricSnapshot(Instant.parse("2024-01-01T00:03:00Z"), 10, 1, 100.0),
            new MetricSnapshot(Instant.parse("2024-01-01T00:04:00Z"), 100, 50, 100.0)
        );


        Map<String, List<Anomaly>> result = service.detectAll(fakeSnapshots);

        assertTrue(result.containsKey("reqs/min"));
        assertTrue(result.containsKey("errorRate"));
        assertNotNull(result.get("reqs/min"));
        assertNotNull(result.get("errorRate"));
    }
}
