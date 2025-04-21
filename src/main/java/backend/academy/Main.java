package backend.academy;

import backend.academy.loganalyzer.alert.AlertManager;
import backend.academy.loganalyzer.alert.TelegramAlertManager;
import backend.academy.loganalyzer.analyzer.DateRangeLogFilter;
import backend.academy.loganalyzer.analyzer.FieldLogFilter;
import backend.academy.loganalyzer.analyzer.LogAnalyzer;
import backend.academy.loganalyzer.anomaly.Anomaly;
import backend.academy.loganalyzer.anomaly.AnomalyConfigurator;
import backend.academy.loganalyzer.anomaly.AnomalyService;
import backend.academy.loganalyzer.anomaly.MetricSnapshot;
import backend.academy.loganalyzer.anomaly.MetricsAggregator;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import backend.academy.loganalyzer.report.LogReportFormat;
import backend.academy.loganalyzer.report.LogReportFormatFactory;
import backend.academy.loganalyzer.template.LogRecord;
import backend.academy.loganalyzer.template.LogResult;
import com.beust.jcommander.JCommander;
import java.time.Duration;
import java.time.Instant;
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
        Instant start = Instant.now();
        LogResult result = getLogResult(config.path(), config.filterField(), config.filterValue(),
            config.from(), config.to());
        Instant end = Instant.now();
        Duration elapsed = Duration.between(start, end);
        log.info("⏱ Анализ занял: {} мс", elapsed.toMillis());
        log.info(formatter.format(result));
        log.debug("TOKEN = {}", System.getenv("TG_TOKEN"));
        log.debug("CHAT  = {}", System.getenv("TG_CHAT"));
    }

    private static AlertManager buildAlertManager() {
        String tok = System.getenv("TG_TOKEN");
        String chat = System.getenv("TG_CHAT");
        if (tok != null && chat != null && !tok.isBlank() && !chat.isBlank()) {
            return new TelegramAlertManager(tok, chat);
        }
        return text -> {
        };
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

            MetricsAggregator aggregator = new MetricsAggregator(Duration.ofSeconds(20));
            List<MetricSnapshot> snapshots = aggregator.aggregate(logs);

            AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
            Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snapshots);

//            log.debug("🧪 anomalies = {}", anomalies);
//            log.debug("🧪 snapshots = {}", snapshots.size());

            if (!anomalies.isEmpty()) {
                anomalies.forEach((metric, list) ->
                    log.warn("⚠ Detected {} anomalies for {} : {}", list.size(), metric, list));
            }

            long totalRequests = analyzer.countTotalRequests(logs);
            double averageSize = analyzer.averageResponseSize(logs);
            Map<String, Long> resourceCounts = analyzer.countResources(logs);
            Map<Integer, Long> statusCodeCounts = analyzer.countStatusCodes(logs);
            double percentile = analyzer.percentile95ResponseSize(logs);

            AlertManager alert = buildAlertManager();
            if (!anomalies.isEmpty()) {
                StringBuilder msg = new StringBuilder("*NginxLogAnalyzer*: обнаружены аномалии\n");
                anomalies.forEach((m, l) -> msg.append("• ").append(m).append(" — ").append(l.size()).append(" шт.\n"));
                alert.send(msg.toString());
            }

            return new LogResult(
                totalRequests,
                averageSize,
                resourceCounts,
                statusCodeCounts,
                percentile,
                anomalies
            );
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }
}
