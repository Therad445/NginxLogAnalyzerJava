package backend.academy;

import backend.academy.loganalyzer.alert.AlertManager;
import backend.academy.loganalyzer.alert.TelegramAlertManager;
import backend.academy.loganalyzer.analyzer.DateRangeLogFilter;
import backend.academy.loganalyzer.analyzer.FieldLogFilter;
import backend.academy.loganalyzer.analyzer.IpAnalyzer;
import backend.academy.loganalyzer.analyzer.LogAnalyzer;
import backend.academy.loganalyzer.analyzer.SuspiciousIpDetector;
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
import java.util.Set;
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
        log.info("⏱ Анализ занял: {} мс", Duration.between(start, end).toMillis());

        try {
            if (result != null) {
                log.info(formatter.format(result));

                if (config.exportJson() != null) {
                    Path p = Path.of(config.exportJson());
                    ResultExporter.toJson(result, p);
                    log.info("💾 Сохранено в JSON: {}", p);
                }
                if (config.exportCsv() != null) {
                    Path p = Path.of(config.exportCsv());
                    ResultExporter.toCsv(result, p);
                    log.info("💾 Сохранено в CSV: {}", p);
                }
            } else {
                log.error("⚠ Ошибка анализа: LogResult == null (возможно, после фильтрации не осталось записей)");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при экспорте отчёта", e);
        }

        log.debug("TOKEN = {}", System.getenv("TG_TOKEN"));
        log.debug("CHAT  = {}", System.getenv("TG_CHAT"));
    }

    private static AlertManager buildAlertManager() {
        String token = System.getenv("TG_TOKEN");
        String chat = System.getenv("TG_CHAT");
        if (token != null && chat != null && !token.isBlank() && !chat.isBlank()) {
            return new TelegramAlertManager(token, chat);
        }
        return new AlertManager() {
            public void send(String text) {
            }

            public void sendImage(File image, String caption) {
            }
        };
    }

    public static LogResult getLogResult(
        Config config,
        String path,
        String filterField,
        String filterValue,
        String from,
        String to
    ) {
        NginxLogParser parser = new NginxLogParser();
        LogAnalyzer analyzer = new LogAnalyzer();

        try {
            Reader reader = ReaderSelector.typeSelector(path);
            Stream<String> lines = reader.read(path);
            List<LogRecord> logs = parser.parse(lines);

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
                log.warn("⚠ После фильтрации логов не осталось.");
                return null;
            }

            // Топ IP по запросам и ошибкам
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

            // Сбор метрик
            MetricsAggregator aggregator = new MetricsAggregator(Duration.ofSeconds(20));
            List<MetricSnapshot> snapshots = aggregator.aggregate(logs);

            AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
            Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snapshots);

            // Выявление подозрительных IP
            SuspiciousIpDetector detector = new SuspiciousIpDetector(Duration.ofSeconds(20), 10);
            Set<String> suspiciousIps = detector.detect(logs);
            if (!suspiciousIps.isEmpty()) {
                log.warn("🚨 Подозрительная активность от IP:");
                suspiciousIps.forEach(ip -> log.warn(" - {}", ip));
            }

            // Telegram уведомление
            AlertManager alert = buildAlertManager();
            if (!anomalies.isEmpty() || !suspiciousIps.isEmpty()) {
                StringBuilder msg = new StringBuilder("*NginxLogAnalyzer*: ");
                if (!anomalies.isEmpty()) {
                    msg.append("обнаружены аномалии\n");
                    anomalies.forEach(
                        (m, l) -> msg.append("• ").append(m).append(" — ").append(l.size()).append(" шт.\n"));
                }
                if (!suspiciousIps.isEmpty()) {
                    msg.append("\n🚨 Подозрительные IP:\n");
                    suspiciousIps.forEach(ip -> msg.append("• ").append(ip).append("\n"));
                }
                alert.send(msg.toString());
            }

            // График
            try {
                String pathToChart = "reports/traffic_errors.png";
                new ChartGenerator().generateTimeSeriesChart(snapshots, pathToChart);
                File chart = new File(pathToChart);
                if (chart.exists()) {
                    alert.sendImage(chart, "📊 График трафика и ошибок");
                    log.info("📊 График сохранён: {}", pathToChart);
                }
            } catch (IOException e) {
                log.error("Ошибка при генерации графика", e);
            }

            return new LogResult(
                analyzer.countTotalRequests(logs),
                analyzer.averageResponseSize(logs),
                analyzer.countResources(logs),
                analyzer.countStatusCodes(logs),
                analyzer.percentile95ResponseSize(logs),
                anomalies,
                suspiciousIps
            );

        } catch (Exception e) {
            log.error("Ошибка анализа логов", e);
        }

        return null;
    }
}
