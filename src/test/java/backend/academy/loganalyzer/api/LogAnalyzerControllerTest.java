package backend.academy.loganalyzer.api;

import backend.academy.loganalyzer.model.LogResult;
import backend.academy.loganalyzer.service.LogAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
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
            .willReturn(new byte[] {1, 2, 3});  // не пустой PDF!

        MockMultipartFile emptyLog = new MockMultipartFile(
            "file", "empty.log", "text/plain", new byte[0]
        );

        mvc.perform(multipart("/analyze/pdf").file(emptyLog))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.pdf\""))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(result -> {
                byte[] content = result.getResponse().getContentAsByteArray();
                assertTrue(content.length > 0, "PDF content should not be empty");
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
}
