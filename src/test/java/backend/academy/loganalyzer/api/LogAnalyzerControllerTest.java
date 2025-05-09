package backend.academy.loganalyzer.api;

import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.service.LogAnalysisService;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogAnalyzerController.class)
class LogAnalyzerControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LogAnalysisService logAnalysisService;

    @Test
    void pingReturnsAliveMessage() throws Exception {
        mvc.perform(get("/analyze/ping"))
            .andExpect(status().isOk())
            .andExpect(content().string("✅ NginxLogAnalyzer API работает"));
    }

    @Test
    void analyzeLogEmptyFileReturnsZeroMetrics() throws Exception {
        given(logAnalysisService.analyze(any(Path.class), eq("json")))
            .willReturn(new LogResult(0, 0.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of()));

        MockMultipartFile emptyLog = new MockMultipartFile(
            "file", "empty.log", "text/plain", new byte[0]
        );

        mvc.perform(multipart("/analyze").file(emptyLog))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalRequests").value(0))
            .andExpect(jsonPath("$.averageResponseSize").value(0.0))
            .andExpect(jsonPath("$.resourceCounts").isMap())
            .andExpect(jsonPath("$.statusCodeCounts").isMap())
            .andExpect(jsonPath("$.percentile").value(0.0))
            .andExpect(jsonPath("$.anomalies").isMap())
            .andExpect(jsonPath("$.suspiciousIps").isArray());
    }

    @Test
    void analyzeLogToPdfEmptyFileReturnsPdfWithHeaders() throws Exception {
        given(logAnalysisService.analyze(any(Path.class), eq("markdown")))
            .willReturn(new LogResult(0, 0.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of()));
        given(logAnalysisService.generatePdfReport(any()))
            .willReturn(new byte[] {1, 2, 3});

        MockMultipartFile emptyLog = new MockMultipartFile(
            "file", "empty.log", "text/plain", new byte[0]
        );

        mvc.perform(multipart("/analyze/pdf").file(emptyLog))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.pdf\""))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(result -> {
                byte[] content = result.getResponse().getContentAsByteArray();
                assertTrue(content.length > 0);
            });
    }

    @Test
    void analyzeToMarkdownEmptyFileReturnsMarkdown() throws Exception {
        given(logAnalysisService.analyze(any(Path.class), eq("markdown")))
            .willReturn(new LogResult(0, 0.0, Map.of(), Map.of(), 0.0, Map.of(), Set.of()));
        given(logAnalysisService.generateMarkdownReport(any()))
            .willReturn("# Report\nGenerated markdown report.");

        MockMultipartFile emptyLog = new MockMultipartFile(
            "file", "empty.log", "text/plain", new byte[0]
        );

        mvc.perform(multipart("/analyze/markdown")
                .file(emptyLog)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/markdown"))
            .andExpect(content().string(startsWith("#")));
    }

    @Test
    void analyzeLog_whenServiceThrows_shouldReturn500() throws Exception {
        given(logAnalysisService.analyze(any(Path.class), eq("json")))
            .willThrow(new RuntimeException("Simulated failure"));

        MockMultipartFile file = new MockMultipartFile("file", "log.log", "text/plain", "GET /".getBytes());

        mvc.perform(multipart("/analyze").file(file))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void analyzeLog_withData_returnsCorrectMetrics() throws Exception {
        given(logAnalysisService.analyze(any(Path.class), eq("json")))
            .willReturn(new LogResult(
                42, 123.45,
                Map.of("/index.html", 12L),
                Map.of(200, 40L, 404, 2L),
                999.9,
                Map.of(),
                Set.of("1.2.3.4")
            ));

        MockMultipartFile file = new MockMultipartFile("file", "access.log", "text/plain", "log data".getBytes());

        mvc.perform(multipart("/analyze").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRequests").value(42))
            .andExpect(jsonPath("$.averageResponseSize").value(123.45))
            .andExpect(jsonPath("$.resourceCounts['/index.html']").value(12))
            .andExpect(jsonPath("$.statusCodeCounts['200']").value(40))
            .andExpect(jsonPath("$.statusCodeCounts['404']").value(2))
            .andExpect(jsonPath("$.percentile").value(999.9))
            .andExpect(jsonPath("$.suspiciousIps[0]").value("1.2.3.4"));
    }
}
