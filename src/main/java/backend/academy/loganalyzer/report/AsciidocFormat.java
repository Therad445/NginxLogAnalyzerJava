package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public class AsciidocFormat implements LogReportFormat {
    @Override
    public String format(LogResult result) {
        formatNullCheck(result);
        String newLine = "\n";
        String startLine = "== Общая информация\n\n"
            + "|===\n| Метрика | Значение" + newLine;
        String endLine = "|===" + newLine;
        return startLine
            + middleLine(result, newLine)
            + endLine;
    }
}
