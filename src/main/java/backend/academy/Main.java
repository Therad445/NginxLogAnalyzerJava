package backend.academy;

import backend.academy.loganalyzer.analyzer.DateRangeLogFilter;
import backend.academy.loganalyzer.analyzer.FieldLogFilter;
import backend.academy.loganalyzer.analyzer.LogAnalyzer;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import backend.academy.loganalyzer.report.LogReportFormat;
import backend.academy.loganalyzer.report.LogReportFormatFactory;
import backend.academy.loganalyzer.template.LogRecord;
import backend.academy.loganalyzer.template.LogResult;
import com.beust.jcommander.JCommander;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;


@Log4j2
@UtilityClass
public class Main {
    public static void main(String[] args) {
        Config config = new Config();
        JCommander.newBuilder().addObject(config).build().parse(args);
        LogReportFormat formatter = LogReportFormatFactory.getLogReportFormat(config.format());
        LogResult result = getLogResult(config.path(), config.filterField(), config.filterValue(),
        config.from(), config.to());
        log.info(formatter.format(result));
    }

    private static LogResult getLogResult(String path, String filterField, String filterValue, String from, String to) {
        NginxLogParser parser = new NginxLogParser();
        LogAnalyzer analyzer = new LogAnalyzer();
        try {
            Reader reader = ReaderSelector.typeSelector(path);
            Stream<String> stringStream = reader.read(path);
            List<LogRecord> logs = parser.parse(stringStream);
            if (filterField != null && filterValue != null) {
                logs = analyzer.applyFilter(logs, new FieldLogFilter(filterField, filterValue));
            }

            if (from != null && to != null) {
                logs = analyzer.applyFilter(logs,
                    new DateRangeLogFilter(LocalDateTime.parse(from), LocalDateTime.parse(to)));
            }

            long totalRequests = analyzer.countTotalRequests(logs);
            double averageSize = analyzer.averageResponseSize(logs);
            Map<String, Long> resourceCounts = analyzer.countResources(logs);
            Map<Integer, Long> statusCodeCounts = analyzer.countStatusCodes(logs);
            double percentile = analyzer.percentile95ResponseSize(logs);

            return new LogResult(totalRequests, averageSize, resourceCounts, statusCodeCounts, percentile);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }
}
