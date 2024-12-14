package backend.academy.loganalyzer.report;

public class LogReportFormatFactory {
    public static LogReportFormat getLogReportFormat(String format) {
        if ("adoc".equalsIgnoreCase(format)) {
            return new AsciidocFormat();
        } else if ("markdows".equalsIgnoreCase(format)) {
            return new MarkdownFormat();
        } else {
            throw new IllegalArgumentException("Неизвенстный формат: " + format);
        }
    }
}
