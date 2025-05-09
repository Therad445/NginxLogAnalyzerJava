package backend.academy.loganalyzer.api;

import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.service.LogAnalysisService;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class LogAnalyzerController {

    private final LogAnalysisService logAnalysisService;

    @PostMapping
    public ResponseEntity<LogResult> analyzeLog(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            LogResult result = logAnalysisService.analyze(tmp, "json");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> analyzeLogToPdf(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            LogResult result = logAnalysisService.analyze(tmp, "markdown");
            byte[] pdfContent = logAnalysisService.generatePdfReport(result);

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"report.pdf\"")
                .header("Content-Type", "application/pdf")
                .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/markdown", produces = "text/markdown")
    public ResponseEntity<String> analyzeToMarkdown(@RequestParam("file") MultipartFile file) {
        try {
            Path tmp = Files.createTempFile("nginx-log-", ".log");
            file.transferTo(tmp);

            LogResult result = logAnalysisService.analyze(tmp, "markdown");
            String markdown = logAnalysisService.generateMarkdownReport(result);

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
