package backend.academy.loganalyzer.report;

import backend.academy.loganalyzer.template.LogResult;

public class MarkdownFormat implements LogReportFormat {

    @Override
    public String format(LogResult result) {
        formatNullCheck(result);
        String newLine = " |\n";
        String startLine = "#### Общая информация\n\n"
            + "| Метрика | Значение" + newLine
            + "| ------- | --------:|\n";
        String endLine = "";
        return startLine
            + middleLine(result, newLine)
            + endLine;
    }
}
