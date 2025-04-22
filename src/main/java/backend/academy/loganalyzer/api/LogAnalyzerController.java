package backend.academy.loganalyzer.api;

import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.template.LogResult;
import backend.academy.Main;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/analyze")
public class LogAnalyzerController {

    @PostMapping
    public ResponseEntity<LogResult> analyzeLog(@RequestParam("file") MultipartFile file) {
        try {
            // Сохраняем лог во временный файл
            Path temp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(temp);

            // Запускаем анализ
            Config config = new Config();
            config.path(temp.toAbsolutePath().toString());
            config.format("json");

            LogResult result = Main.getLogResult(
                config,
                config.path(), null, null, null, null
            );

            if (result == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> analyzeLogToPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Временный файл
            Path temp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(temp);

            // Анализ
            Config config = new Config();
            config.path(temp.toAbsolutePath().toString());
            config.format("markdown");
            LogResult result = Main.getLogResult(config, config.path(), null, null, null, null);
            if (result == null) {
                return ResponseEntity.badRequest().build();
            }

            // Генерация PDF во временный файл
            Path pdf = Files.createTempFile("report-", ".pdf");
            new PdfReportGenerator().generate(result, pdf.toString());

            // Читаем PDF в байты
            byte[] content = Files.readAllBytes(pdf);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"report.pdf\"")
                .header("Content-Type", "application/pdf")
                .body(content);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

