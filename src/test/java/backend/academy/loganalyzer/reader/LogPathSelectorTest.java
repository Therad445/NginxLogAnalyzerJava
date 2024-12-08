package backend.academy.loganalyzer.reader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Stream;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class LogPathSelectorTest {

    @Test
    void testTypeSelector_withValidFilePath() throws IOException {
        // Arrange
        String path = "test.log"; // Файл должен существовать!!

        // Act
        Stream<String> result = LogPathSelector.typeSelector(path);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testTypeSelector_withEmptyPath() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            LogPathSelector.typeSelector("");
        });
        assertEquals("Путь пустой", exception.getMessage());
    }

    @Test
    void testCheckPath_withNoSuchFile() {
        // Act & Assert
        NoSuchFileException exception = assertThrows(NoSuchFileException.class, () -> {
            LogPathSelector.typeSelector("nonexistent.log");
        });
        assertTrue(exception.getMessage().contains("Нет файла по пути:"));
    }
}
