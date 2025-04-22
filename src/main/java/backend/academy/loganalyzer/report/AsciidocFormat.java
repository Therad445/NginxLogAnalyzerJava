package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public class AsciidocFormat implements LogReportFormat {

    @Override
    public String format(LogResult result) {
        formatNullCheck(result);

        String nl = "\n";
        StringBuilder sb = new StringBuilder();

        /* ---- Общие метрики ---- */
        sb.append("== Общая информация\n\n")
            .append("|===\n| Метрика | Значение").append(nl)
            .append(middleLine(result, nl))
            .append("|===\n");

        /* ---- АНОМАЛИИ ---- */
        sb.append("\n== Аномалии\n\n");
        if (result.anomalies().isEmpty()) {
            sb.append("* Не обнаружены").append(nl);
        } else {
            result.anomalies().forEach((metric, list) ->
                sb.append(String.format("* %s — %d шт.%s", metric, list.size(), nl)));
        }

        /* ---- ПОДОЗРИТЕЛЬНЫЕ IP ---- */
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
