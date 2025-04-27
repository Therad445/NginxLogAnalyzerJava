package backend.academy.loganalyzer.visual;

import backend.academy.loganalyzer.anomaly.MetricSnapshot;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChartGeneratorTest {

    private final ChartGenerator generator = new ChartGenerator();

    @TempDir
    File tempDir;

    @Test
    void generate_createsPngFileWithContent() throws IOException {
        var now = Instant.now();
        MetricSnapshot s1 = new MetricSnapshot(now, 10L, 2L, 0.0, 0.0, 0L);
        MetricSnapshot s2 = new MetricSnapshot(now.plusSeconds(60), 20L, 5L, 0.0, 0.0, 0L);
        String outputPath = new File(tempDir, "chart.png").getAbsolutePath();

        generator.generateTimeSeriesChart(List.of(s1, s2), outputPath);

        File outFile = new File(outputPath);
        assertThat(outFile)
            .exists()
            .as("Файл должен быть создан")
            .isFile();
        assertThat(outFile.length())
            .isGreaterThan(0)
            .as("Файл должен содержать бинарные данные PNG");
    }

    @Test
    void generate_throwsWhenNoDataPoints() {
        List<MetricSnapshot> emptyList = List.of();
        String outputPath = new File(tempDir, "empty.png").getAbsolutePath();

        assertThatThrownBy(() -> generator.generateTimeSeriesChart(emptyList, outputPath))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Y-Axis data cannot be empty");
    }
}
