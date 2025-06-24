package io.github.therad445.loganalyzer.analyzer;

import io.github.therad445.loganalyzer.model.HttpMethod;
import io.github.therad445.loganalyzer.model.LogRecord;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuspiciousIpDetectorTest {

    private final static LocalDateTime BASE_TIME = LocalDateTime.of(2025, 5, 9, 12, 0);
    private List<LogRecord> logs;

    @BeforeEach
    void setup() {
        logs = new ArrayList<>();
    }

    private LogRecord createRecord(String ip, LocalDateTime time) {
        return new LogRecord(
            ip, "-", time, HttpMethod.GET,
            "/test", 200, 100, "-", "agent"
        );
    }

    @Test
    void detect_singleIpAboveThreshold_shouldBeMarkedSuspicious() {
        Duration window = Duration.ofMinutes(60);
        int threshold = 5;
        SuspiciousIpDetector detector = new SuspiciousIpDetector(window, threshold);

        for (int i = 0; i < 6; i++) {
            logs.add(createRecord("192.168.1.1", BASE_TIME.plusMinutes(i)));
        }

        Set<String> result = detector.detect(logs);

        assertEquals(Set.of("192.168.1.1"), result);
    }

    @Test
    void detect_multipleIpsAndWindows_shouldDetectSeparately() {
        Duration window = Duration.ofMinutes(60);
        SuspiciousIpDetector detector = new SuspiciousIpDetector(window, 4);

        logs.add(createRecord("10.0.0.1", BASE_TIME.plusMinutes(0)));
        logs.add(createRecord("10.0.0.1", BASE_TIME.plusMinutes(10)));
        logs.add(createRecord("10.0.0.1", BASE_TIME.plusMinutes(20)));

        for (int i = 0; i < 4; i++) {
            logs.add(createRecord("10.0.0.2", BASE_TIME.plusHours(1).plusMinutes(i * 10)));
        }

        Set<String> result = detector.detect(logs);

        assertEquals(Set.of("10.0.0.2"), result);
    }


    @Test
    void detect_ipBelowThreshold_shouldBeIgnored() {
        SuspiciousIpDetector detector = new SuspiciousIpDetector(Duration.ofMinutes(30), 3);

        logs.add(createRecord("8.8.8.8", BASE_TIME));
        logs.add(createRecord("8.8.8.8", BASE_TIME.plusMinutes(1)));

        Set<String> result = detector.detect(logs);
        assertTrue(result.isEmpty());
    }

    @Test
    void detect_emptyList_shouldReturnEmptySet() {
        SuspiciousIpDetector detector = new SuspiciousIpDetector(Duration.ofMinutes(10), 1);
        Set<String> result = detector.detect(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void detect_exactThreshold_shouldIncludeIp() {
        SuspiciousIpDetector detector = new SuspiciousIpDetector(Duration.ofMinutes(15), 3);

        logs.add(createRecord("1.1.1.1", BASE_TIME));
        logs.add(createRecord("1.1.1.1", BASE_TIME.plusMinutes(1)));
        logs.add(createRecord("1.1.1.1", BASE_TIME.plusMinutes(2)));

        Set<String> result = detector.detect(logs);
        assertTrue(result.contains("1.1.1.1"));
    }
}
