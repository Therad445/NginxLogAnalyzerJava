package io.github.therad445.loganalyzer.anomaly;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyServiceTest {

    private static MetricSnapshot ms(String minute, long value) {
        Instant ts = Instant.parse("2024-01-01T" + minute + ":00Z");
        return new MetricSnapshot(ts, value, 0, 0.0, 0.0, 0);
    }

    @Test
    void mergesResultsFromMultipleDetectors() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 10), ms("00:04", 10), ms("00:05", 100)
        );

        AnomalyDetector z = new ZScoreAnomalyDetector("z", ms -> (double) ms.requests(), 5, 2.0);
        AnomalyDetector ewma = new EwmaAnomalyDetector("ewma", ms -> (double) ms.requests(), 0.5, 1.0);

        AnomalyService service = new AnomalyService(List.of(z, ewma));
        Map<String, List<Anomaly>> all = service.detectAll(snapshots);

        assertTrue(all.containsKey("z"));
        assertTrue(all.containsKey("ewma"));
        assertEquals(1, all.get("z").size());
        assertEquals(1, all.get("ewma").size());
    }

    @Test
    void handlesEmptyDetectorListGracefully() {
        List<MetricSnapshot> snapshots = List.of(ms("00:00", 10), ms("00:01", 15));
        AnomalyService service = new AnomalyService(List.of());
        Map<String, List<Anomaly>> all = service.detectAll(snapshots);
        assertTrue(all.isEmpty());
    }
}



