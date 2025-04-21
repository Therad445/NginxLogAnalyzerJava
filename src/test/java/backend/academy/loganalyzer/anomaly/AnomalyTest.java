package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnomalyTest {

    @Test
    void recordHoldsValuesCorrectly() {
        Instant ts = Instant.parse("2024-01-01T00:00:00Z");
        Anomaly a = new Anomaly(ts, "metricX", 99.0, 80.0, 3.5);

        assertEquals(ts, a.timestamp());
        assertEquals("metricX", a.metric());
        assertEquals(99.0, a.value());
        assertEquals(80.0, a.threshold());
        assertEquals(3.5, a.score());
    }

    @Test
    void anomaliesWithSameDataAreEqual() {
        Instant ts = Instant.parse("2024-01-01T00:00:00Z");
        Anomaly a1 = new Anomaly(ts, "m", 1.0, 2.0, 3.0);
        Anomaly a2 = new Anomaly(ts, "m", 1.0, 2.0, 3.0);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
