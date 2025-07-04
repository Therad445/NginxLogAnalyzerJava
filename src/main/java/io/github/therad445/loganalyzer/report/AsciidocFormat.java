package io.github.therad445.loganalyzer.report;

import io.github.therad445.loganalyzer.model.LogResult;

public class AsciidocFormat implements LogReportFormat {

    @Override
    public String format(LogResult result) {
        formatNullCheck(result);

        String nl = "\n";
        StringBuilder sb = new StringBuilder();

        sb.append("== Общая информация\n\n")
            .append("|===\n| Метрика | Значение").append(nl)
            .append(middleLine(result, nl))
            .append("|===\n");

        sb.append("\n== Аномалии\n\n");
        if (result.anomalies().isEmpty()) {
            sb.append("* Не обнаружены").append(nl);
        } else {
            result.anomalies().forEach((metric, list) ->
                sb.append(String.format("* %s — %d шт.%s", metric, list.size(), nl)));
        }

        sb.append("\n== Подозрительные IP\n\n");
        if (result.suspiciousIps() == null || result.suspiciousIps().isEmpty()) {
            sb.append("* Не обнаружены").append(nl);
        } else {
            result.suspiciousIps().forEach(ip ->
                sb.append("* ").append(ip).append(nl));
        }

        return sb.toString();
    }
}
