package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public class AsciidocFormat implements LogReportFormat {
    @Override
    public String format(LogResult result) {
        return "== Общая информация\n\n" +
            "|===\n| Метрика | Значение\n" +
            "| Количество запросов | " + result.totalRequests() + "\n" +
            "| Средний размер ответа | " + result.averageResponseSize() + "\n|===\n";
    }
}
