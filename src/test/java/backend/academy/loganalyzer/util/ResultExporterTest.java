package backend.academy.loganalyzer.util;

import backend.academy.loganalyzer.anomaly.Anomaly;
import backend.academy.loganalyzer.model.LogResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultExporterTest {

    private LogResult sampleResult() {
        return new LogResult(
            100,
            512.5,
            Map.of("/index.html", 50L),
            Map.of(200, 80L, 404, 20L),
            2048.0,
            Map.of("errorRate", List.of(
                new Anomaly(Instant.parse("2024-01-01T00:00:00Z"), "errorRate", 0.25, 0.1, 1.5)
            )),
            Set.of("192.168.1.1")
        );
    }

    @Test
    void toJson_writesPrettyPrintedJson(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("result.json");
        ResultExporter.toJson(sampleResult(), file);

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("\"totalRequests\" : 100"));
        assertTrue(content.contains("\"errorRate\""));
    }

    @Test
    void toCsv_writesStructuredCsv(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("result.csv");
        ResultExporter.toCsv(sampleResult(), file);

        assertTrue(Files.exists(file));
        String content = Files.readString(file);

        assertTrue(content.contains("Метрика,Значение"));
        assertTrue(content.contains("Количество запросов,100"));
        assertTrue(content.contains("95%-й перцентиль размера,2048,00"));
        assertTrue(content.contains("Код ответа,Кол-во"));
        assertTrue(content.contains("200,80"));
        assertTrue(content.contains("Аномалий"));
        assertTrue(content.contains("errorRate,1"));
    }

    @Test
    void toJson_createsParentDirectories(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path subdir = tempDir.resolve("nested/dir/out.json");
        ResultExporter.toJson(sampleResult(), subdir);
        assertTrue(Files.exists(subdir));
    }

    @Test
    void toCsv_emptyDataStillProducesValidCsv(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        LogResult empty = new LogResult(0, 0.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of());
        Path file = tempDir.resolve("empty.csv");

        ResultExporter.toCsv(empty, file);
        String content = Files.readString(file);

        assertTrue(content.contains("Количество запросов,0"));
        assertTrue(content.contains("Код ответа,Кол-во"));
        assertTrue(content.contains("Метрика,Аномалий"));
    }
}
