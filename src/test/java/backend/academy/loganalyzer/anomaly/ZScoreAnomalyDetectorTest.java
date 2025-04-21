package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZScoreAnomalyDetectorTest {

    @Test
    void detectsClearHighOutlierWhenStdPositive() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00"), ms("00:01"), ms("00:02"), ms("00:03"), ms("00:04"), ms("00:05", 100)
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 2.0);
        var anomalies = detector.detect(snapshots);
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.get(0).score() > 2.0);
    }

    @Test
    void detectsLowOutlierWhenStdPositive() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 100), ms("00:01", 100), ms("00:02", 100), ms("00:03", 100), ms("00:04", 100), ms("00:05", 5)
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 2.0);
        var anomalies = detector.detect(snapshots);
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.get(0).score() > 2.0);
        assertEquals(5.0, anomalies.get(0).value());
    }

    @Test
    void detectsOutlierEvenIfStdIsZero() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 10), ms("00:04", 10), ms("00:05", 99)
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 2.0);
        var anomalies = detector.detect(snapshots);
        assertEquals(1, anomalies.size());
        assertEquals(Double.POSITIVE_INFINITY, anomalies.get(0).score());
    }

    @Test
    void doesNotTriggerOnFlatData() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00"), ms("00:01"), ms("00:02"), ms("00:03"), ms("00:04"), ms("00:05")
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 2.0);
        var anomalies = detector.detect(snapshots);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void returnsEmptyListIfHistoryTooShort() {
        List<MetricSnapshot> shortList = List.of(
            ms("00:00"), ms("00:01"), ms("00:02")
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 2.0);
        var anomalies = detector.detect(shortList);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void detectsMultipleConsecutiveAnomalies() {
        List<MetricSnapshot> snapshots = List.of(
            ms("00:00", 10), ms("00:01", 10), ms("00:02", 10), ms("00:03", 10), ms("00:04", 10),
            ms("00:05", 90), ms("00:06", 85), ms("00:07", 10)
        );
        var detector = new ZScoreAnomalyDetector("reqs/min", ms -> (double) ms.requests(), 5, 1.5);
        var anomalies = detector.detect(snapshots);
        assertEquals(2, anomalies.size());
        assertTrue(anomalies.get(0).value() > 80);
        assertTrue(anomalies.get(1).value() > 80);
    }

    private static MetricSnapshot ms(String minute) {
        return ms(minute, 10);
    }

    private static MetricSnapshot ms(String minute, long value) {
        Instant ts = Instant.parse("2024-01-01T" + minute + ":00Z");
        return new MetricSnapshot(ts, value, 0, 0.0);
    }
}
