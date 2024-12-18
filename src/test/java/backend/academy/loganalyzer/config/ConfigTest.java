package backend.academy.loganalyzer.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testValidConfig() {
        String[] args = {
            "--path", "/logs",
            "--from", "2024-01-01T00:00:00",
            "--to", "2024-12-31T23:59:59",
            "--format", "markdown",
            "--filter-field", "agent",
            "--filter-value", "Mozilla"
        };

        Config config = new Config();
        JCommander.newBuilder().addObject(config).build().parse(args);

        assertEquals("/logs", config.path());
        assertEquals("2024-01-01T00:00:00", config.from());
        assertEquals("2024-12-31T23:59:59", config.to());
        assertEquals("markdown", config.format());
        assertEquals("agent", config.filterField());
        assertEquals("Mozilla", config.filterValue());
    }

    @Test
    void testMissingRequiredPath() {
        // Arrange
        String[] args = {"--from", "2024-01-01", "--to", "2024-01-31"};
        Config config = new Config();
        JCommander jCommander = JCommander.newBuilder().addObject(config).build();

        // Act & Assert
        ParameterException exception = assertThrows(ParameterException.class, () -> jCommander.parse(args));
        assertEquals("The following option is required: [--path]", exception.getMessage());
    }
}
