package backend.academy.loganalyzer.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

class ConfigTest {

    @BeforeEach
    void clearSystemProperties() {
        System.clearProperty("windowSeconds");
        System.clearProperty("zThreshold");
    }

    @AfterEach
    void resetSystemProperties() {
        System.clearProperty("windowSeconds");
        System.clearProperty("zThreshold");
    }

    @Test
    void getAggregationWindow_DefaultsTo20Seconds() {
        assertEquals(Duration.ofSeconds(20), Config.getAggregationWindow());
    }

    @Test
    void getAggregationWindow_RespectsSystemProperty() {
        System.setProperty("windowSeconds", "45");
        assertEquals(Duration.ofSeconds(45), Config.getAggregationWindow());
    }

    @Test
    void getZThreshold_DefaultsTo3() {
        assertEquals(3.0, Config.getZThreshold());
    }

    @Test
    void getZThreshold_RespectsSystemProperty() {
        System.setProperty("zThreshold", "2.5");
        assertEquals(2.5, Config.getZThreshold());
    }
}
