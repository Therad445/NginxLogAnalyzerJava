package io.github.therad445.loganalyzer.model;

import io.github.therad445.loganalyzer.anomaly.Anomaly;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogResultTest {

    @Test
    void validInput_shouldCreateCorrectInstance() {
        Map<String, Long> resources = Map.of("/index.html", 100L);
        Map<Integer, Long> statusCodes = Map.of(200, 900L);
        Map<String, List<Anomaly>> anomalies = Map.of(
            "size", List.of(
                new Anomaly(Instant.parse("2025-05-09T10:15:30Z"), "size", 1000.0, 500.0, 2.0),
                new Anomaly(Instant.parse("2025-05-09T10:16:00Z"), "size", 1200.0, 500.0, 2.4)
            )
        );
        Set<String> suspiciousIps = Set.of("192.168.0.1");

        LogResult result = new LogResult(
            1000,
            512.0,
            resources,
            statusCodes,
            1024.0,
            anomalies,
            suspiciousIps
        );

        assertEquals(1000, result.totalRequests());
        assertEquals(512.0, result.averageResponseSize());
        assertEquals(resources, result.resourceCounts());
        assertEquals(statusCodes, result.statusCodeCounts());
        assertEquals(1024.0, result.percentile());
        assertEquals(anomalies, result.anomalies());
        assertEquals(suspiciousIps, result.suspiciousIps());
    }

    @Test
    void nullAnomalies_shouldDefaultToEmptyMap() {
        LogResult result = new LogResult(
            500,
            256.0,
            Map.of("/ping", 50L),
            Map.of(404, 50L),
            512.0,
            null,
            Set.of()
        );

        assertNotNull(result.anomalies());
        assertTrue(result.anomalies().isEmpty());
    }

    @Test
    void negativeTotalRequests_shouldThrowIllegalArgumentException() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
            new LogResult(-1, 1.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of())
        );
        assertEquals("totalRequests меньше нуля", e.getMessage());
    }

    @Test
    void negativeAverageSize_shouldThrowIllegalArgumentException() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
            new LogResult(1, -100.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of())
        );
        assertEquals("averageResponseSize меньше нуля", e.getMessage());
    }

    @Test
    void nullResourceCounts_shouldThrowNullPointerException() {
        Exception e = assertThrows(NullPointerException.class, () ->
            new LogResult(1, 1.0, null, Map.of(), 0.0, Map.of(), Set.of())
        );
        assertEquals("resourceCounts пустой", e.getMessage());
    }

    @Test
    void nullStatusCodeCounts_shouldThrowNullPointerException() {
        Exception e = assertThrows(NullPointerException.class, () ->
            new LogResult(1, 1.0, Map.of(), null, 0.0, Map.of(), Set.of())
        );
        assertEquals("statusCodeCounts пустой", e.getMessage());
    }

    @Test
    void negativePercentile_shouldThrowIllegalArgumentException() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
            new LogResult(1, 1.0, Map.of(), Map.of(), -1.0, Map.of(), Set.of())
        );
        assertEquals("percentile меньше нуля", e.getMessage());
    }

    @Test
    void emptyCollections_shouldBeStoredCorrectly() {
        LogResult result = new LogResult(
            0,
            0.0,
            Map.of(),
            Map.of(),
            0.0,
            Map.of(),
            Set.of()
        );

        assertEquals(0, result.totalRequests());
        assertEquals(0.0, result.averageResponseSize());
        assertTrue(result.resourceCounts().isEmpty());
        assertTrue(result.statusCodeCounts().isEmpty());
        assertTrue(result.anomalies().isEmpty());
        assertTrue(result.suspiciousIps().isEmpty());
    }
}
