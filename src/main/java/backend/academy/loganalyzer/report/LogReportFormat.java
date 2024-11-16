package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

/**
 * LogReportFormatter и его реализации - форматированиегенерация отчетов в разных форматах.
 */
public interface LogReportFormat {
    String format(LogResult result);
}
