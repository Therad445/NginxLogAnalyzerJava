package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogReportFormatTest {
    @Test
    void middleLine_withNullResult() {
        // Arrange
        LogResult result = null;
        String newLine = null;
        // Act
        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception = assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));
        //Assert
        assertEquals("Переданы пустые переменные", exception.getMessage());
    }

    @Test
    void middleLine_withNullNewLine() {
        // Arrange
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5
        );
        String newLine = null;
        // Act
        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception = assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));
        //Assert
        assertEquals("Передан пустой элемент переноса", exception.getMessage());
    }

    @Test
    void middleLine_withEmptyNewLine() {
        // Arrange
        LogResult result = new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L, "/about.html", 25L),
            Map.of(200, 80L, 404, 20L),
            201.5
        );
        String newLine = "";
        // Act
        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception = assertThrows(NullPointerException.class, () -> logReportFormat.middleLine(result, newLine));
        //Assert
        assertEquals("Передан пустой элемент переноса", exception.getMessage());
    }

    @Test
    void formatNullCheck_withNull() {
        // Arrange
        LogResult result = null;
        // Act
        LogReportFormat logReportFormat = new LogReportFormat() {
            @Override
            public String format(LogResult result) {
                return "";
            }
        };
        Exception exception = assertThrows(NullPointerException.class, () -> logReportFormat.formatNullCheck(result));
        //Assert
        assertEquals("Переданы пустые переменные", exception.getMessage());
    }
}
