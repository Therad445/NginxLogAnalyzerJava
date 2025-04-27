package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public interface LogReportFormat {
    String format(LogResult result);

    default void formatNullCheck(LogResult result) {
        if (result == null) {
            throw new NullPointerException("Переданы пустые переменные");
        }
    }

    default String middleLine(LogResult result, String newLine) {
        formatNullCheck(result);
        if (newLine == null || newLine.isEmpty()) {
            throw new NullPointerException("Передан пустой элемент переноса");
        }
        return "| Количество запросов | " + result.totalRequests() + newLine
            + "| Средний размер ответа | " + result.averageResponseSize() + newLine
            + "| 95% перцентиль размера ответа | " + result.percentile() + newLine;
    }
}
