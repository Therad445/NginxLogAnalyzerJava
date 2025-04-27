package backend.academy.loganalyzer.config;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void aggregationWindow_DefaultsTo20Seconds() {
        assertEquals(Duration.ofSeconds(20), Config.aggregationWindow());
    }

    @Test
    void aggregationWindow_RespectsSystemProperty() {
        System.setProperty("windowSeconds", "45");
        assertEquals(Duration.ofSeconds(45), Config.aggregationWindow());
    }

    @Test
    void zThreshold_DefaultsTo3() {
        assertEquals(3.0, Config.zThreshold());
    }

    @Test
    void zThreshold_RespectsSystemProperty() {
        System.setProperty("zThreshold", "2.5");
        assertEquals(2.5, Config.zThreshold());
    }
}
