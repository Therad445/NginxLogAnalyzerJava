package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

/**
 * ReportGenerator.java и его реализации
 * (MarkdownReportGenerator, AsciidocReportGenerator) —
 * генерация отчетов в разных форматах.
 */
public class MarkdownFormat implements LogReportFormat {
    @Override
    public String format(LogResult result) {
        return "#### Общая информация\n\n" +
            "| Метрика | Значение |\n" +
            "|---------|---------:|\n" +
            "| Количество запросов | " + result.totalRequests() + " |\n" +
            "| Средний размер ответа | " + result.averageResponseSize() + " |\n";
    }
}
