package io.github.therad445.loganalyzer.anomaly;

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
        Anomaly a = anomalies.get(0);
        assertEquals(30.0, a.value());
        assertEquals("reqs/min", a.metric());
        assertEquals(Instant.parse("2024-01-01T00:03:00Z"), a.timestamp());
        assertTrue(a.score() > 1.0);
    }

    @Test
    void detectsSharpDropBelowMean() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 30), ms("00:01", 30), ms("00:02", 30), ms("00:03", 30), ms("00:04", 10)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 0.5);

        List<Anomaly> anomalies = detector.detect(snapshots);

        Anomaly anomaly = anomalies.get(0);
        assertEquals(10.0, anomaly.value());
        assertEquals("reqs/min", anomaly.metric());
        assertEquals(Instant.parse("2024-01-01T00:04:00Z"), anomaly.timestamp());
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

        assertTrue(detector.detect(snapshots).isEmpty());
        assertTrue(detector.detect(List.of()).isEmpty());
    }

    @Test
    void detectsMultipleAnomalies() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 30),
            ms("00:03", 10), ms("00:04", 35)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 1.0);

        List<Anomaly> anomalies = detector.detect(snapshots);

        assertEquals(2, anomalies.size());
        assertEquals(30.0, anomalies.get(0).value());
        assertEquals(35.0, anomalies.get(1).value());
    }

    @Test
    void ignoresWhenStdDevIsZero() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 10)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.3, 1.0);

        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void smallFluctuationsShouldNotTrigger() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 100), ms("00:01", 101), ms("00:02", 99), ms("00:03", 98),
            ms("00:04", 100), ms("00:05", 97), ms("00:06", 103)
        );
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.2, 2.5);

        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void longStableDataShouldNotTrigger() {
        List<MetricSnapshot> snapshots = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            snapshots.add(ms(String.format("00:%02d", i % 60), 20));
        }
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector("reqs/min",
            ms -> (double) ms.requests(), 0.1, 3.0);

        List<Anomaly> anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }
}
