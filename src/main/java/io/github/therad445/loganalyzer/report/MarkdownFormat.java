package io.github.therad445.loganalyzer.report;

import io.github.therad445.loganalyzer.model.LogResult;

public class MarkdownFormat implements LogReportFormat {
    @Override
    public String format(LogResult result) {
        formatNullCheck(result);

        String nl = " |\n";
        StringBuilder sb = new StringBuilder();

        sb.append("#### Общая информация\n\n")
            .append("| Метрика | Значение").append(nl)
            .append("| ------- | --------:|\n")
            .append(middleLine(result, nl));

        sb.append("\n#### Аномалии\n\n");
        if (result.anomalies().isEmpty()) {
            sb.append("* Не обнаружены\n");
        } else {
            result.anomalies().forEach((metric, list) ->
                sb.append(String.format("* %s — %d шт.\n", metric, list.size())));
        }

        if (result.suspiciousIps() != null && !result.suspiciousIps().isEmpty()) {
            sb.append("\n#### Подозрительные IP\n\n");
            result.suspiciousIps().forEach(ip -> sb.append("* ").append(ip).append("\n"));
        }

        return sb.toString();
    }
}

