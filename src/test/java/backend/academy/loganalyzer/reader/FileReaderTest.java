package backend.academy.loganalyzer.reader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {

    private static final String NODIRECT = "nodirect";
    private static final String EMPTY_FILE = "src/main/resources/empty.txt";

    @Test
    public void testReadValidFile() throws IOException {
        // Arrange
        Reader fileReader = new FileReader();
        String testFilePath = EMPTY_FILE;
        Path path = Path.of(testFilePath);
        Files.write(path, List.of("Line 1", "Line 2", "Line 3"));

        // Act
        List<String> lines = fileReader.read(testFilePath).toList();

        // Assert
        assertFalse(lines.isEmpty());
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 3", lines.get(2));
        Files.delete(path);
    }

    @Test
    public void testReadNonExistentFile() {
        // Arrange
        FileReader fileReader = new FileReader();
        String invalidFilePath = NODIRECT;

        // Act & Assert
        assertThrows(IOException.class, () -> fileReader.read(invalidFilePath).collect(Collectors.toList()));
    }
}
