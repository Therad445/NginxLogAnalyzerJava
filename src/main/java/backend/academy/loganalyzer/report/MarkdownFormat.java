package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public class MarkdownFormat implements LogReportFormat {

    @Override
    public String format(LogResult result) {
        formatNullCheck(result);

        String nl = " |\n";
        StringBuilder sb = new StringBuilder();

        /* ---- Общие метрики ---- */
        sb.append("#### Общая информация\n\n")
            .append("| Метрика | Значение").append(nl)
            .append("| ------- | --------:|\n")
            .append(middleLine(result, nl));

        /* ---- АНОМАЛИИ ---- */
        sb.append("\n#### Аномалии\n\n");
        if (result.anomalies().isEmpty()) {
            sb.append("* Не обнаружены\n");
        } else {
            result.anomalies().forEach((metric, list) ->
                sb.append(String.format("* %s — %d шт.\n", metric, list.size())));
        }

        return sb.toString();
    }
}
