package backend.academy.loganalyzer.service;

import backend.academy.loganalyzer.analyzer.LogAnalyzer;
import backend.academy.loganalyzer.analyzer.SuspiciousIpDetector;
import backend.academy.loganalyzer.anomaly.AnomalyConfigurator;
import backend.academy.loganalyzer.anomaly.MetricsAggregator;
import backend.academy.loganalyzer.model.LogRecord;
import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.visual.PdfReportGenerator;
import backend.academy.loganalyzer.report.LogReportFormatFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogAnalysisService {

    private static final int CHART_WINDOWS = 5;

    public LogResult analyze(Path logFilePath, String format) throws Exception {
        Config config = new Config();
        config.source("file");
        config.streamingMode(false);
        config.path(logFilePath.toString());
        config.format(format);

        Reader reader = ReaderSelector.select(config);
        List<String> lines = reader.read().toList();
        List<LogRecord> logs = new NginxLogParser().parse(lines.stream());

        MetricsAggregator aggregator = new MetricsAggregator(
            Config.aggregationWindow(), CHART_WINDOWS
        );
        var snapshots = aggregator.aggregate(logs);

        var anomalies = AnomalyConfigurator.defaultService().detectAll(snapshots);
        var suspiciousIps = new SuspiciousIpDetector(Config.aggregationWindow(), 10)
            .detect(logs);

        LogAnalyzer analyzer = new LogAnalyzer();
        return new LogResult(
            analyzer.countTotalRequests(logs),
            analyzer.averageResponseSize(logs),
            analyzer.countResources(logs),
            analyzer.countStatusCodes(logs),
            analyzer.percentile95ResponseSize(logs),
            anomalies,
            suspiciousIps
        );
    }

    public byte[] generatePdfReport(LogResult result) throws Exception {
        Path pdfPath = Files.createTempFile("report-", ".pdf");
        new PdfReportGenerator().generate(result, pdfPath.toString());
        return Files.readAllBytes(pdfPath);
    }

    public String generateMarkdownReport(LogResult result) {
        return LogReportFormatFactory.getLogReportFormat("markdown").format(result);
    }
}
