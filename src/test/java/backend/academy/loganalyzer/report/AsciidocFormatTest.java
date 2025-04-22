package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsciidocFormatTest {

    @Test
    void testFormat_withValidLogResult() {
        // Arrange
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5,
            Map.of(),
            Set.of()
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
            | 95% перцентиль размера ответа | 201.5
            |===

            == Аномалии

            * Не обнаружены
            """;
        assertEquals(expected, output);
    }

    @Test
    void testFormat_withNullRequests() {
        // Arrange
        AsciidocFormat formatter = new AsciidocFormat();

        // Act & Assert
        Exception ex = assertThrows(NullPointerException.class,
            () -> formatter.format(null));
        assertEquals("Переданы пустые переменные", ex.getMessage());
    }
}
