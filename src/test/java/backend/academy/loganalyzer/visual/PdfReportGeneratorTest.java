package backend.academy.loganalyzer.visual;

import backend.academy.loganalyzer.anomaly.Anomaly;
import backend.academy.loganalyzer.template.LogResult;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfReportGeneratorTest {

    private final PdfReportGenerator generator = new PdfReportGenerator();
    @TempDir
    Path tempDir;

    @Test
    void generate_createsNonEmptyPdf_whenNoAnomaliesAndNoSuspiciousIps() throws Exception {
        LogResult result = new LogResult(
            100L,
            512.0,
            Map.of(),
            Map.of(),
            768.0,
            Map.of(),
            Set.of()
        );
        Path output = tempDir.resolve("report-empty.pdf");

        generator.generate(result, output.toString());

        assertThat(Files.exists(output))
            .as("PDF должен быть создан")
            .isTrue();
        assertThat(Files.size(output))
            .as("PDF не должен быть пустым")
            .isGreaterThan(0);
    }

    @Test
    void generate_createsPdfWithAnomaliesAndIps() throws Exception {
        Instant now = Instant.now();
        Anomaly anomaly1 = new Anomaly(now, "reqsPerWindow", 123.0, 10.0, 5.0);
        Anomaly anomaly2 = new Anomaly(now, "errorRate", 0.05, 0.02, 2.5);

        LogResult result = new LogResult(
            42L,
            123.45,
            Map.of(),
            Map.of(),
            200.0,
            Map.of(
                "reqsPerWindow", List.of(anomaly1),
                "errorRate", List.of(anomaly2, anomaly2)
            ),
            Set.of("192.168.0.1", "10.0.0.5")
        );
        Path output = tempDir.resolve("report-full.pdf");

        generator.generate(result, output.toString());

        assertThat(Files.exists(output)).isTrue();
        assertThat(Files.size(output)).isGreaterThan(0);
    }

    @Test
    void generate_throwsWhenOutputPathIsDirectory() {
        // Arrange
        LogResult result = new LogResult(
            1L,
            1.0,
            Map.of(),
            Map.of(),
            0.0,
            Map.of(),
            Set.of()
        );
        Path dir = tempDir.resolve("outDir");
        dir.toFile().mkdir();

        // Act & Assert
        assertThatThrownBy(() -> generator.generate(result, dir.toString()))
            .isInstanceOfAny(FileNotFoundException.class, java.io.IOException.class);
    }
}
