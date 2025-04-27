//package backend.academy.loganalyzer.anomaly;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class AnomalyConfiguratorTest {
//
//    @Test
//    void defaultServiceFindsAnomaliesForBothMetrics() {
//
//        AnomalyService service = AnomalyConfigurator.defaultService();
//
//        List<MetricSnapshot> fakeSnapshots = List.of(
//            new MetricSnapshot(Instant.parse("2024-01-01T00:00:00Z"), 10, 1, 100.0, 0.1, 1),
//            new MetricSnapshot(Instant.parse("2024-01-01T00:01:00Z"), 10, 1, 100.0, 0.1, 1),
//            new MetricSnapshot(Instant.parse("2024-01-01T00:02:00Z"), 10, 1, 100.0, 0.1, 1),
//            new MetricSnapshot(Instant.parse("2024-01-01T00:03:00Z"), 10, 1, 100.0, 0.1, 1),
//            new MetricSnapshot(Instant.parse("2024-01-01T00:04:00Z"), 100, 50, 100.0, 100.0, 100)
//        );
//
//        Map<String, List<Anomaly>> result = service.detectAll(fakeSnapshots);
//
//        assertTrue(result.containsKey("errorRate"),    "Ожидали ключ 'errorRate'");
//        assertTrue(result.containsKey("reqsPerWindow"), "Ожидали ключ 'reqsPerWindow'");
//
//        assertFalse(result.get("reqsPerWindow").isEmpty(),
//            "Ожидали хотя бы одну аномалию по 'reqsPerWindow'");
//        assertFalse(result.get("errorRate").isEmpty(),
//            "Ожидали хотя бы одну аномалию по 'errorRate'");
//    }
//}
