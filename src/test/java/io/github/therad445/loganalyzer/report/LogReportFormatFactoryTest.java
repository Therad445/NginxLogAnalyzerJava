package io.github.therad445.loganalyzer.report;

import org.junit.jupiter.api.Test;
import static io.github.therad445.loganalyzer.report.LogReportFormatFactory.getLogReportFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogReportFormatFactoryTest {

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsAdoc() {

        String format = "adoc";

        LogReportFormat logReportFormat = getLogReportFormat(format);

        assertEquals(AsciidocFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsMarkdown() {

        String format = "markdown";

        LogReportFormat logReportFormat = getLogReportFormat(format);

        assertEquals(MarkdownFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsNull() {

        String format = null;

        LogReportFormat logReportFormat = getLogReportFormat(format);

        assertEquals(AsciidocFormat.class, logReportFormat.getClass());
    }

    @Test
    void getLogReportFormatFactory_ShouldReturnMarkdownFormat_WhenFormatIsIncorrect() {

        String format = "mistake";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> getLogReportFormat(format));

        assertEquals("Неизвенстный формат: " + format, exception.getMessage());
    }
}
