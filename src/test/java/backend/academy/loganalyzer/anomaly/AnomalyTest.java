package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyTest {

    @Test
    void anomaly_shouldStoreAllFieldsCorrectly() {
        Instant ts = Instant.parse("2025-05-09T16:00:00Z");
        Anomaly a = new Anomaly(ts, "responseSize", 1024.0, 512.0, 2.0);

        assertEquals(ts, a.timestamp());
        assertEquals("responseSize", a.metric());
        assertEquals(1024.0, a.value());
        assertEquals(512.0, a.threshold());
        assertEquals(2.0, a.score());
    }

    @Test
    void anomaly_equalsAndHashCode_shouldWork() {
        Instant ts = Instant.now();
        Anomaly a1 = new Anomaly(ts, "latency", 300.0, 100.0, 3.0);
        Anomaly a2 = new Anomaly(ts, "latency", 300.0, 100.0, 3.0);
        Anomaly a3 = new Anomaly(ts, "latency", 301.0, 100.0, 3.0);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a3);
    }

    @Test
    void anomaly_toString_shouldContainAllFields() {
        Instant ts = Instant.parse("2025-05-09T12:00:00Z");
        Anomaly a = new Anomaly(ts, "requests", 1000.0, 100.0, 10.0);
        String s = a.toString();

        assertTrue(s.contains("2025-05-09T12:00:00Z"));
        assertTrue(s.contains("requests"));
        assertTrue(s.contains("1000.0"));
        assertTrue(s.contains("100.0"));
        assertTrue(s.contains("10.0"));
    }
}
