package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EwmaAnomalyDetectorTest {

    private static MetricSnapshot ms(String minute, long value) {
        Instant ts = Instant.parse("2024-01-01T" + minute + ":00Z");
        return new MetricSnapshot(ts, value, 0, 0.0, 0.0, 0);
    }

    @Test
    void detectsSharpSpikeAboveMean() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 30)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 1.0);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertEquals(1, anomalies.size());
        assertEquals(30.0, anomalies.get(0).value());
    }

    @Test
    void detectsSharpDropBelowMean() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 30), ms("00:01", 30), ms("00:02", 30), ms("00:03", 30), ms("00:04", 10)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 0.5);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertEquals(1, anomalies.size());
        assertEquals(10.0, anomalies.get(0).value());
    }

    @Test
    void doesNotTriggerOnStableData() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 20), ms("00:01", 21), ms("00:02", 22), ms("00:03", 23)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 3.0);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void returnsEmptyIfLessThanTwoPoints() {
        List<MetricSnapshot> snapshots = List.of(ms("00:00", 10));
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.5, 2.0);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void detectsMultipleAnomalies() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 30), ms("00:03", 10), ms("00:04", 35)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 1.0);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertEquals(2, anomalies.size());
    }

    @Test
    void ignoresWhenStdIsZeroAndXCloseToMu() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 10)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 1.0);
        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }
}
