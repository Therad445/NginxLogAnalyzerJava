package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.anomaly.Anomaly;
import backend.academy.loganalyzer.template.LogResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownFormatTest {

    @Test
    void testFormat_withValidLogResult() {
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5,
            Map.of(),
            Set.of()
        );
        MarkdownFormat formatter = new MarkdownFormat();

        String output = formatter.format(result);

        String expected = """
            #### Общая информация

            | Метрика | Значение |
            | ------- | --------:|
            | Количество запросов | 100 |
            | Средний размер ответа | 512.5 |
            | 95% перцентиль размера ответа | 201.5 |

            #### Аномалии

            * Не обнаружены
            """;
        assertEquals(expected, output);
    }

    @Test
    void testFormat_withNullRequests() {
        LogResult result = null;
        MarkdownFormat formatter = new MarkdownFormat();
        Exception exception = assertThrows(NullPointerException.class, () -> formatter.format(result));
        assertEquals("Переданы пустые переменные", exception.getMessage());
    }

    @Test
    void testFormat_withDetectedAnomalies() {
        LogResult result = new LogResult(
            50,
            250.0,
            Map.of("/home", 30L),
            Map.of(200, 50L),
            123.0,
            Map.of(
                "reqs/min", List.of(new Anomaly(Instant.now(), "reqs/min", 300, 100, 2.5)),
                "errors/sec", List.of(new Anomaly(Instant.now(), "errors/sec", 50, 10, 4.2))
            ),
            Set.of()
        );
        MarkdownFormat formatter = new MarkdownFormat();
        String output = formatter.format(result);

        assertTrue(output.contains("* reqs/min — 1 шт."));
        assertTrue(output.contains("* errors/sec — 1 шт."));

    }
}
