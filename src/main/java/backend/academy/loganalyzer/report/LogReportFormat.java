package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

/**
 * LogReportFormatter - форматирование отчета в markdown и adoc.
 */
public interface LogReportFormat {
    String format(LogResult result);
}
