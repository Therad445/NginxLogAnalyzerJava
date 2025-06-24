package io.github.therad445.loganalyzer.service;

import io.github.therad445.loganalyzer.analyzer.LogAnalyzer;
import io.github.therad445.loganalyzer.analyzer.SuspiciousIpDetector;
import io.github.therad445.loganalyzer.anomaly.AnomalyConfigurator;
import io.github.therad445.loganalyzer.anomaly.MetricsAggregator;
import io.github.therad445.loganalyzer.config.Config;
import io.github.therad445.loganalyzer.model.LogRecord;
import io.github.therad445.loganalyzer.model.LogResult;
import io.github.therad445.loganalyzer.parser.NginxLogParser;
import io.github.therad445.loganalyzer.reader.Reader;
import io.github.therad445.loganalyzer.reader.ReaderSelector;
import io.github.therad445.loganalyzer.report.LogReportFormatFactory;
import io.github.therad445.loganalyzer.visual.PdfReportGenerator;
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
