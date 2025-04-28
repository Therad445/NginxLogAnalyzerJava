package backend.academy.loganalyzer.model;

import backend.academy.loganalyzer.anomaly.Anomaly;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogResultTest {

    @Test
    void LogResultTest_IsValid() {
        long totalRequests = 100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = 201.5;
        Map<String, List<Anomaly>> anomalies = Map.of();
        Set<String> suspiciousIps = Set.of();

        LogResult logResult = new LogResult(
            totalRequests, averageResponseSize,
            resourceCounts, statusCodeCounts,
            percentile, anomalies, suspiciousIps);

        assertEquals(totalRequests, logResult.totalRequests());
        assertEquals(averageResponseSize, logResult.averageResponseSize());
        assertEquals(resourceCounts, logResult.resourceCounts());
        assertEquals(statusCodeCounts, logResult.statusCodeCounts());
        assertEquals(percentile, logResult.percentile());
        assertEquals(anomalies, logResult.anomalies());
    }

    @Test
    void totalRequests_IsNegative() {
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> new LogResult(-1, 10,
                Map.of("x", 1L), Map.of(200, 1L),
                0.0, Map.of(), Set.of()));
        assertEquals("totalRequests меньше нуля", ex.getMessage());
    }

    @Test
    void averageResponseSize_IsNegative() {
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> new LogResult(1, -5,
                Map.of("x", 1L), Map.of(200, 1L),
                0.0, Map.of(), Set.of()));
        assertEquals("averageResponseSize меньше нуля", ex.getMessage());
    }

    @Test
    void resourceCounts_IsNull() {
        Exception ex = assertThrows(NullPointerException.class,
            () -> new LogResult(1, 10,
                null, Map.of(200, 1L),
                0.0, Map.of(), Set.of()));
        assertEquals("resourceCounts пустой", ex.getMessage());
    }

    @Test
    void statusCodeCounts_IsNull() {
        Exception ex = assertThrows(NullPointerException.class,
            () -> new LogResult(1, 10,
                Map.of("x", 1L), null,
                0.0, Map.of(), Set.of()));
        assertEquals("statusCodeCounts пустой", ex.getMessage());
    }

    @Test
    void percentile_IsNegative() {
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> new LogResult(1, 10,
                Map.of("x", 1L), Map.of(200, 1L),
                -0.1, Map.of(), Set.of()));
        assertEquals("percentile меньше нуля", ex.getMessage());
    }
}
