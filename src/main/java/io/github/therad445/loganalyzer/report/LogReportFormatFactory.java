package io.github.therad445.loganalyzer.report;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogReportFormatFactory {
    public static LogReportFormat getLogReportFormat(String format) {
        if ("adoc".equalsIgnoreCase(format) || format == null) {
            return new AsciidocFormat();
        } else if ("markdown".equalsIgnoreCase(format)) {
            return new MarkdownFormat();
        } else {
            throw new IllegalArgumentException("Неизвенстный формат: " + format);
        }
    }
}
