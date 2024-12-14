package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

/**
 * LogReportFormatter и его реализации - форматированиегенерация отчетов в разных форматах.
 */
public interface LogReportFormat {
    String format(LogResult result);
    default String middleLine(LogResult result, String newLine) {
        return "| Количество запросов | " + result.totalRequests() + newLine
            + "| Средний размер ответа | " + result.averageResponseSize() + newLine
            + "| 95% перцентиль размера ответа | " + result.percentile() + newLine;
    }
}
