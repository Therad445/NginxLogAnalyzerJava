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
import backend.academy.loganalyzer.visual.PdfReportGenerator;
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

    public static void main(String[] args) throws IOException {
        // 1. Разбор аргументов
        Config config = new Config();
        JCommander.newBuilder()
            .addObject(config)
            .build()
            .parse(args);

        // 2. Выбор ридера
        Reader reader = ReaderSelector.select(config);

        // 3. Стриминг или батч
        if (config.streamingMode()) {
            runStreaming(reader, config);
        } else {
            runBatch(reader, config);
        }
    }

    private static void runStreaming(Reader reader, Config config) {
        NginxLogParser parser = new NginxLogParser();
        MetricsAggregator aggregator = new MetricsAggregator(
            Config.aggregationWindow(), 5
        );
        AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
        SuspiciousIpDetector ipDetector =
            new SuspiciousIpDetector(Config.aggregationWindow(), 10);
        AlertManager alert = buildAlertManager();
        ChartGenerator chartGen = new ChartGenerator();

        // reader.read принимает Consumer<String>
        try {
            reader.read(line -> {
                try {
                    // 1) Парсим строку в LogRecord
                    LogRecord record = parser.parse(Stream.of(line)).get(0);

                    // 2) Агрегация + аномалии
                    List<MetricSnapshot> snaps = aggregator.addAndAggregate(record);
                    Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snaps);

                    // 3) Подозрительные IP
                    Set<String> suspiciousIps = ipDetector.detect(List.of(record));

                    // 4) Оповещения
                    if (!anomalies.isEmpty() || !suspiciousIps.isEmpty()) {
                        StringBuilder msg = new StringBuilder("*Аномалии на потоке:*\n");
                        anomalies.forEach((m, list) ->
                            msg.append("• ").append(m).append(": ").append(list.size()).append("\n"));
                        suspiciousIps.forEach(ip ->
                            msg.append("🚨 ").append(ip).append("\n"));
                        alert.send(msg.toString());
                    }

                    // 5) График раз в 5 окон
                    if (aggregator.shouldEmitChart()) {
                        String path = "output/stream_chart.png";
                        chartGen.generateTimeSeriesChart(snaps, path);
                        alert.sendImage(new File(path), "📊 Стриминговый график");
                    }

                } catch (Exception ex) {
                    log.error("Ошибка в стриминге", ex);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runBatch(Reader reader, Config config) {
        Instant start = Instant.now();

        // Считываем все строки
        List<String> lines = null;
        try {
            lines = reader.read().toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Парсим в объекты
        NginxLogParser parser = new NginxLogParser();
        List<LogRecord> logs = parser.parse(lines.stream());

        LogAnalyzer analyzer = new LogAnalyzer();

        // Фильтрация
        if (config.filterField() != null && config.filterValue() != null) {
            logs = analyzer.applyFilter(
                logs,
                new FieldLogFilter(config.filterField(), config.filterValue())
            );
        }
        if (config.filterIp() != null) {
            logs = analyzer.applyFilter(
                logs,
                new FieldLogFilter("remoteAddr", config.filterIp())
            );
        }
        if (config.from() != null && config.to() != null) {
            logs = analyzer.applyFilter(
                logs,
                new DateRangeLogFilter(
                    LocalDateTime.parse(config.from()),
                    LocalDateTime.parse(config.to())
                )
            );
        }

        if (logs.isEmpty()) {
            log.warn("⚠ После фильтрации логов не осталось записей.");
            return;
        }

        // Основной анализ
        IpAnalyzer ipAnalyzer = new IpAnalyzer();
        Map<String, Long> requestsPerIp = ipAnalyzer.countRequestsPerIp(logs);
        Map<String, Long> errorsPerIp = ipAnalyzer.countErrorsPerIp(logs);

        log.info("📌 Топ 5 IP по запросам:");
        requestsPerIp.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> log.info("  {} → {} запросов", e.getKey(), e.getValue()));

        log.info("📌 Топ 5 IP по ошибкам:");
        errorsPerIp.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> log.info("  {} → {} ошибок", e.getKey(), e.getValue()));

        // Сбор метрик и аномалий
        MetricsAggregator aggregator = new MetricsAggregator(
            Config.aggregationWindow(), 5  // 5 окон до графика
        );
        List<MetricSnapshot> snapshots = aggregator.aggregate(logs);
        AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
        Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snapshots);

        SuspiciousIpDetector ipDetector =
            new SuspiciousIpDetector(Config.aggregationWindow(), 10);
        Set<String> suspiciousIps = ipDetector.detect(logs);

        // Уведомления
        AlertManager alert = buildAlertManager();
        if (!anomalies.isEmpty() || !suspiciousIps.isEmpty()) {
            StringBuilder msg = new StringBuilder("*NginxLogAnalyzer*: найденные аномалии\n");
            anomalies.forEach((m, list) ->
                msg.append("• ").append(m).append(": ").append(list.size()).append("\n"));
            if (!suspiciousIps.isEmpty()) {
                msg.append("🚨 Подозрительные IP:\n");
                suspiciousIps.forEach(ip -> msg.append("• ").append(ip).append("\n"));
            }
            alert.send(msg.toString());
        }

        // Генерация графика
        try {
            String chartPath = "output/report_chart.png";
            new ChartGenerator().generateTimeSeriesChart(snapshots, chartPath);
            File img = new File(chartPath);
            if (img.exists()) {
                alert.sendImage(img, "📊 График трафика и ошибок");
                log.info("📊 График сохранён: {}", chartPath);
            }
        } catch (IOException e) {
            log.error("Ошибка генерации графика", e);
        }

        // Форматирование и экспорт
        LogReportFormat formatter =
            LogReportFormatFactory.getLogReportFormat(config.format());
        LogResult result = new LogResult(
            analyzer.countTotalRequests(logs),
            analyzer.averageResponseSize(logs),
            analyzer.countResources(logs),
            analyzer.countStatusCodes(logs),
            analyzer.percentile95ResponseSize(logs),
            anomalies,
            suspiciousIps
        );
        log.info(formatter.format(result));

        try {
            if (config.exportJson() != null) {
                Path p = Path.of(config.exportJson());
                ResultExporter.toJson(result, p);
                log.info("💾 JSON сохранён: {}", p);
            }
            if (config.exportCsv() != null) {
                Path p = Path.of(config.exportCsv());
                ResultExporter.toCsv(result, p);
                log.info("💾 CSV сохранён: {}", p);
            }
            new PdfReportGenerator().generate(result, "output/report.pdf");
            log.info("📄 PDF-отчёт: output/report.pdf");
        } catch (IOException ex) {
            log.error("Ошибка при экспорте результатов", ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("⏱ Анализ занял: {} мс",
            Duration.between(Instant.now().minusMillis(0), Instant.now()).toMillis());
    }

    private static AlertManager buildAlertManager() {
        String token = System.getenv("TG_TOKEN");
        String chat = System.getenv("TG_CHAT");
        if (token != null && chat != null && !token.isBlank() && !chat.isBlank()) {
            return new TelegramAlertManager(token, chat);
        }
        return new AlertManager() {
            @Override public void send(String text) {
            }

            @Override public void sendImage(File image, String caption) {
            }
        };
    }
}
