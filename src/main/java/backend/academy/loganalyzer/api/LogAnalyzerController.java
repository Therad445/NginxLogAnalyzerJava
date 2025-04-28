package backend.academy.loganalyzer.api;

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
import backend.academy.loganalyzer.report.LogReportFormatFactory;
import backend.academy.loganalyzer.model.LogRecord;
import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.visual.PdfReportGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/analyze")
public class LogAnalyzerController {

    private static final int CHART_WINDOWS = 5;

    @PostMapping
    public ResponseEntity<LogResult> analyzeLog(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            Config config = new Config();
            config.source("file");
            config.streamingMode(false);
            config.path(tmp.toString());
            config.format("json");

            Reader reader = ReaderSelector.select(config);
            List<String> lines = reader.read().toList();

            var parser = new NginxLogParser();
            List<LogRecord> logs = parser.parse(lines.stream());

            MetricsAggregator agg = new MetricsAggregator(
                Config.aggregationWindow(), CHART_WINDOWS
            );
            List<MetricSnapshot> snaps = agg.aggregate(logs);
            AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
            Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snaps);

            SuspiciousIpDetector ipDet = new SuspiciousIpDetector(
                Config.aggregationWindow(), 10
            );
            Set<String> suspiciousIps = ipDet.detect(logs);

            LogAnalyzer analyzer = new LogAnalyzer();
            LogResult result = new LogResult(
                analyzer.countTotalRequests(logs),
                analyzer.averageResponseSize(logs),
                analyzer.countResources(logs),
                analyzer.countStatusCodes(logs),
                analyzer.percentile95ResponseSize(logs),
                anomalies,
                suspiciousIps
            );

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> analyzeLogToPdf(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            Config config = new Config();
            config.source("file");
            config.streamingMode(false);
            config.path(tmp.toString());
            config.format("markdown");

            Reader reader = ReaderSelector.select(config);
            List<String> lines = reader.read().toList();
            List<LogRecord> logs = new NginxLogParser().parse(lines.stream());

            MetricsAggregator agg = new MetricsAggregator(
                Config.aggregationWindow(), CHART_WINDOWS
            );
            List<MetricSnapshot> snaps = agg.aggregate(logs);
            Map<String, List<Anomaly>> anomalies = AnomalyConfigurator
                .defaultService()
                .detectAll(snaps);
            Set<String> suspiciousIps = new SuspiciousIpDetector(
                Config.aggregationWindow(), 10
            ).detect(logs);

            LogAnalyzer analyzer = new LogAnalyzer();
            LogResult result = new LogResult(
                analyzer.countTotalRequests(logs),
                analyzer.averageResponseSize(logs),
                analyzer.countResources(logs),
                analyzer.countStatusCodes(logs),
                analyzer.percentile95ResponseSize(logs),
                anomalies,
                suspiciousIps
            );

            Path pdf = Files.createTempFile("report-", ".pdf");
            new PdfReportGenerator().generate(result, pdf.toString());
            byte[] content = Files.readAllBytes(pdf);

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"report.pdf\"")
                .header("Content-Type", "application/pdf")
                .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/markdown", produces = "text/markdown")
    public ResponseEntity<String> analyzeToMarkdown(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            Config config = new Config();
            config.source("file");
            config.streamingMode(false);
            config.path(tmp.toString());
            config.format("markdown");

            Reader reader = ReaderSelector.select(config);
            List<String> lines = reader.read().toList();
            List<LogRecord> logs = new NginxLogParser().parse(lines.stream());

            MetricsAggregator agg = new MetricsAggregator(
                Config.aggregationWindow(), CHART_WINDOWS
            );
            List<MetricSnapshot> snaps = agg.aggregate(logs);
            Map<String, List<Anomaly>> anomalies = AnomalyConfigurator
                .defaultService()
                .detectAll(snaps);
            Set<String> suspiciousIps = new SuspiciousIpDetector(
                Config.aggregationWindow(), 10
            ).detect(logs);

            LogAnalyzer analyzer = new LogAnalyzer();
            LogResult result = new LogResult(
                analyzer.countTotalRequests(logs),
                analyzer.averageResponseSize(logs),
                analyzer.countResources(logs),
                analyzer.countStatusCodes(logs),
                analyzer.percentile95ResponseSize(logs),
                anomalies,
                suspiciousIps
            );

            String markdown = LogReportFormatFactory
                .getLogReportFormat("markdown")
                .format(result);

            return ResponseEntity.ok(markdown);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ping")
    public String ping() {
        return "✅ NginxLogAnalyzer API работает";
    }
}
