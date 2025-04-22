package backend.academy;

import backend.academy.loganalyzer.alert.AlertManager;
import backend.academy.loganalyzer.alert.TelegramAlertManager;
import backend.academy.loganalyzer.analyzer.DateRangeLogFilter;
import backend.academy.loganalyzer.analyzer.FieldLogFilter;
import backend.academy.loganalyzer.analyzer.IpAnalyzer;
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
import backend.academy.loganalyzer.util.ResultExporter;
import backend.academy.loganalyzer.visual.ChartGenerator;
import com.beust.jcommander.JCommander;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
        LogResult result = getLogResult(config, config.path(), config.filterField(), config.filterValue(),
            config.from(), config.to());
        Instant end = Instant.now();
        Duration elapsed = Duration.between(start, end);
        log.info("⏱ Анализ занял: {} мс", elapsed.toMillis());
        try {
            if (result != null) {
                log.info(formatter.format(result));

                if (config.exportJson() != null) {
                    Path p = Path.of(config.exportJson());
                    ResultExporter.toJson(result, p);
                    log.info("💾 Результат сохранён в JSON: {}", p);
                }
                if (config.exportCsv() != null) {
                    Path p = Path.of(config.exportCsv());
                    ResultExporter.toCsv(result, p);
                    log.info("💾 Результат сохранён в CSV: {}", p);
                }
            } else {
                log.error("⚠ Ошибка анализа: LogResult == null (возможно, после фильтрации не осталось записей)");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.debug("TOKEN = {}", System.getenv("TG_TOKEN"));
        log.debug("CHAT  = {}", System.getenv("TG_CHAT"));
    }

    private static AlertManager buildAlertManager() {
        String tok = System.getenv("TG_TOKEN");
        String chat = System.getenv("TG_CHAT");
        if (tok != null && chat != null && !tok.isBlank() && !chat.isBlank()) {
            return new TelegramAlertManager(tok, chat);
        }
        return new AlertManager() {
            @Override
            public void send(String text) {
            }

            @Override
            public void sendImage(File image, String caption) {
            }
        };
    }
    private static LogResult getLogResult(Config config, String path, String filterField, String filterValue, String from, String to) {
        NginxLogParser parser = new NginxLogParser();
        LogAnalyzer analyzer = new LogAnalyzer();
        try {
            Reader reader = ReaderSelector.typeSelector(path);
            Stream<String> stringStream = reader.read(path);
            List<LogRecord> logs = parser.parse(stringStream);
            if (filterField != null && filterValue != null) {
                logs = analyzer.applyFilter(logs, new FieldLogFilter(filterField, filterValue));
            }
            if (config.filterIp() != null) {
                logs = analyzer.applyFilter(logs, new FieldLogFilter("remoteAddr", config.filterIp()));
            }
            if (from != null && to != null) {
                logs = analyzer.applyFilter(logs,
                    new DateRangeLogFilter(LocalDateTime.parse(from), LocalDateTime.parse(to)));
            }

            if (logs.isEmpty()) {
                log.warn("⚠ После фильтрации логи пусты. Прерываем анализ.");
                return null;
            }

            IpAnalyzer ipAnalyzer = new IpAnalyzer();
            Map<String, Long> requestsPerIp = ipAnalyzer.countRequestsPerIp(logs);
            Map<String, Long> errorsPerIp = ipAnalyzer.countErrorsPerIp(logs);

            log.info("📌 Топ 5 IP-адресов по количеству запросов:");
            requestsPerIp.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> log.info("{} → {} запросов", e.getKey(), e.getValue()));

            log.info("📌 Топ 5 IP-адресов по количеству ошибок:");
            errorsPerIp.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> log.info("{} → {} ошибок", e.getKey(), e.getValue()));


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
            try {
                new ChartGenerator().generateTimeSeriesChart(snapshots, "reports/traffic_errors.png");
                log.info("📊 График сохранён: reports/traffic_errors.png");
                File chart = new File("reports/traffic_errors.png");
                if (chart.exists()) {
                    alert.sendImage(chart, "📊 График трафика и ошибок");
                }
            } catch (IOException e) {
                log.error("Не удалось сгенерировать график", e);
            }

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
