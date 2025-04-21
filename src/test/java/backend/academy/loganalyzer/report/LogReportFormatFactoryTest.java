package backend.academy.loganalyzer.report;

import org.junit.jupiter.api.Test;
import static backend.academy.loganalyzer.report.LogReportFormatFactory.getLogReportFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogReportFormatFactoryTest {

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsAdoc() {
        //Arrange
        String format = "adoc";
        //Act
        LogReportFormat logReportFormat = getLogReportFormat(format);
        //Assert
        assertEquals(AsciidocFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsMarkdown() {
        //Arrange
        String format = "markdown";
        //Act
        LogReportFormat logReportFormat = getLogReportFormat(format);
        //Assert
        assertEquals(MarkdownFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsNull() {
        //Arrange
        String format = null;
        //Act
        LogReportFormat logReportFormat = getLogReportFormat(format);
        //Assert
        assertEquals(AsciidocFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsIncorrect() {
        //Arrange
        String format = "mistake";
        //Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> getLogReportFormat(format));
        //Assert
        assertEquals("Неизвенстный формат: " + format, exception.getMessage());
    }
}
