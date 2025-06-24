package io.github.therad445.loganalyzer.report;

import io.github.therad445.loganalyzer.model.LogResult;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogReportFormatTest {
    @Test
    void middleLine_withNullResult() {

        LogResult result = null;
        String newLine = null;

        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception =
            assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));

        assertEquals("Переданы пустые переменные", exception.getMessage());
    }

    @Test
    void middleLine_withNullNewLine() {

        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5,
            Map.of(),
            Set.of()
        );
        String newLine = null;

        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception =
            assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));

        assertEquals("Передан пустой элемент переноса", exception.getMessage());
    }

    @Test
    void middleLine_withEmptyNewLine() {

        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5,
            Map.of(),
            Set.of()
        );
        String newLine = "";

        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception =
            assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));
        assertEquals("Передан пустой элемент переноса", exception.getMessage());
    }

    @Test
    void formatNullCheck_withNull() {

        LogResult result = null;

        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception = assertThrows(NullPointerException.class, () -> logReportFormat.formatNullCheck(result));

        assertEquals("Переданы пустые переменные", exception.getMessage());
    }
}
