package backend.academy.loganalyzer;

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
import backend.academy.loganalyzer.model.LogRecord;
import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import backend.academy.loganalyzer.report.LogReportFormat;
import backend.academy.loganalyzer.report.LogReportFormatFactory;
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
import static backend.academy.loganalyzer.alert.buildAlertManager.buildAlertManager;
import static backend.academy.loganalyzer.util.RunBatch.runBatch;
import static backend.academy.loganalyzer.util.RunStreaming.runStreaming;

@Log4j2
@UtilityClass
public class Main {

    public static void main(String[] args) throws IOException {

        Config config = new Config();
        JCommander.newBuilder()
            .addObject(config)
            .build()
            .parse(args);

        Reader reader = ReaderSelector.select(config);

        if (config.streamingMode()) {
            runStreaming(reader);
        } else {
            runBatch(reader, config);
        }
    }
}
