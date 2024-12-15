package backend.academy.loganalyzer.reader;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class URLReaderTest {

    private static final String GOOGLE_COM = "https://www.google.com";
    private static final String NODIRECT = "https://invalid.url";

    @Test
    public void testReadValidURL() throws IOException {
        // Arrange
        URLReader urlReader = new URLReader();
        String testUrl = GOOGLE_COM;

        // Act
        List<String> lines = urlReader.read(testUrl).toList();

        // Assert
        assertFalse(lines.isEmpty());
    }

    @Test
    public void testReadInvalidURL() {
        // Arrange
        URLReader urlReader = new URLReader();
        String invalidUrl = NODIRECT;

        // Act & Assert
        assertThrows(IOException.class, () -> urlReader.read(invalidUrl).collect(Collectors.toList()));
    }
}
