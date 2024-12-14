package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownFormatTest {

    @Test
    void testFormat_withValidLogResult() {
        // Arrange
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5
        );
        MarkdownFormat formatter = new MarkdownFormat();

        // Act
        String output = formatter.format(result);

        // Assert
        String expected = """
            #### Общая информация

            | Метрика | Значение |
            | ------- | --------:|
            | Количество запросов | 100 |
            | Средний размер ответа | 512.5 |
            | 95% перцентиль размера ответа | 201.5 |
            """;
        assertEquals(expected, output);
    }

    @Test
    void testFormat_withNullRequests() {
        // Arrange
        LogResult result = null;
        MarkdownFormat formatter = new MarkdownFormat();
        //Act
        Exception exception = assertThrows(NullPointerException.class, () -> formatter.format(result));
        //Assert
        assertEquals("Передано пустое значение", exception.getMessage());
    }
}

