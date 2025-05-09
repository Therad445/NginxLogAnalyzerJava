package backend.academy.loganalyzer.service;

import backend.academy.loganalyzer.anomaly.AnomalyConfigurator;
import backend.academy.loganalyzer.anomaly.AnomalyService;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.report.LogReportFormat;
import backend.academy.loganalyzer.report.LogReportFormatFactory;
import backend.academy.loganalyzer.visual.PdfReportGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class LogAnalysisServiceTest {

    private LogAnalysisService service;

    @BeforeEach
    void setup() {
        service = new LogAnalysisService();
    }

    @Test
    void analyze_withValidLogFile_returnsValidLogResult(@TempDir Path tempDir) throws Exception {
        Path log = tempDir.resolve("access.log");
        Files.writeString(log, """
            127.0.0.1 - - [10/May/2024:13:55:36 +0000] "GET /index.html HTTP/1.1" 200 512 "-" "Mozilla"
            127.0.0.1 - - [10/May/2024:13:55:37 +0000] "GET /404 HTTP/1.1" 404 128 "-" "Mozilla"
            """);

        try (MockedStatic<AnomalyConfigurator> ac = mockStatic(AnomalyConfigurator.class);
             MockedStatic<LogReportFormatFactory> f = mockStatic(LogReportFormatFactory.class);
             MockedStatic<PdfReportGenerator> pdfGen = mockStatic(PdfReportGenerator.class)) {

            AnomalyService anomalyService = mock(AnomalyService.class);
            ac.when(AnomalyConfigurator::defaultService).thenReturn(anomalyService);
            when(anomalyService.detectAll(any())).thenReturn(Map.of());

            try (MockedStatic<Config> configStatic = mockStatic(Config.class, CALLS_REAL_METHODS)) {
                configStatic.when(Config::aggregationWindow).thenReturn(java.time.Duration.ofMinutes(1));
                configStatic.when(Config::zThreshold).thenReturn(2.0);
            }

            LogResult result = service.analyze(log, "json");

            assertEquals(2, result.totalRequests());
            assertEquals(320.0, result.averageResponseSize());
            assertEquals(2, result.resourceCounts().size());
            assertEquals(2, result.statusCodeCounts().size());
            assertEquals(512, result.percentile()); // max
            assertTrue(result.anomalies().isEmpty());
        }
    }

    @Test
    void generatePdfReport_createsNonEmptyFile(@TempDir Path tempDir) throws Exception {
        LogResult dummy = new LogResult(1, 1.0, Map.of(), Map.of(), 1.0, Map.of(), Set.of());
        byte[] pdfBytes = service.generatePdfReport(dummy);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateMarkdownReport_returnsExpectedContent() {
        LogResult dummy = new LogResult(1, 1.0, Map.of(), Map.of(), 1.0, Map.of(), Set.of());
        LogReportFormat fakeFormatter = result -> "# Markdown";

        try (MockedStatic<LogReportFormatFactory> factory = mockStatic(LogReportFormatFactory.class)) {
            factory.when(() -> LogReportFormatFactory.getLogReportFormat("markdown")).thenReturn(fakeFormatter);

            String report = service.generateMarkdownReport(dummy);
            assertTrue(report.startsWith("# Markdown"));
        }
    }
}
