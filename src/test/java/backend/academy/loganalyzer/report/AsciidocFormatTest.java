package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AsciidocFormatTest {

    @Test
    void testFormat_withValidLogResult() {
        // Arrange
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L)
        );
        AsciidocFormat formatter = new AsciidocFormat();

        // Act
        String output = formatter.format(result);

        // Assert
        String expected = """
            == Общая информация

            |===
            | Метрика | Значение
            | Количество запросов | 100
            | Средний размер ответа | 512.5
            |===
            """;
        assertEquals(expected, output);
    }

    @Test
    void testFormat_withZeroRequests() {
        // Arrange
        LogResult result = new LogResult(
            0,
            0.0,
            Map.of(),
            Map.of()
        );
        AsciidocFormat formatter = new AsciidocFormat();

        // Act
        String output = formatter.format(result);

        // Assert
        String expected = """
            == Общая информация

            |===
            | Метрика | Значение
            | Количество запросов | 0
            | Средний размер ответа | 0.0
            |===
            """;
        assertEquals(expected, output);
    }

    @Test
    void testFormat_withNegativeValues() {
        // Arrange
        LogResult result = new LogResult(
            -1,
            -10.5,
            Map.of("/error.html", -5L),
            Map.of(500, -3L)
        );
        AsciidocFormat formatter = new AsciidocFormat();

        // Act
        String output = formatter.format(result);

        // Assert
        String expected = """
            == Общая информация

            |===
            | Метрика | Значение
            | Количество запросов | -1
            | Средний размер ответа | -10.5
            |===
            """;
        assertEquals(expected, output);
    }
}
