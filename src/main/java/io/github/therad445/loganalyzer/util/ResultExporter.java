package io.github.therad445.loganalyzer.util;

import io.github.therad445.loganalyzer.model.LogResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ResultExporter {
    private static final ObjectMapper M = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT);

    public static void toJson(LogResult result, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (Writer w = Files.newBufferedWriter(file)) {
            M.writeValue(w, result);
        }
    }

    public static void toCsv(LogResult r, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println("Метрика,Значение");
            pw.printf("Количество запросов,%d%n", r.totalRequests());
            pw.printf("Средний размер ответа,%.2f%n", r.averageResponseSize());
            pw.printf("95%%-й перцентиль размера,%.2f%n", r.percentile());
            pw.println();
            pw.println("Код ответа,Кол-во");
            for (Map.Entry<Integer, Long> e : r.statusCodeCounts().entrySet()) {
                pw.printf("%d,%d%n", e.getKey(), e.getValue());
            }
            pw.println();
            pw.println("Метрика,Аномалий");
            r.anomalies().forEach((metric, list) ->
                pw.printf("%s,%d%n", metric, list.size())
            );
        }
    }
}
