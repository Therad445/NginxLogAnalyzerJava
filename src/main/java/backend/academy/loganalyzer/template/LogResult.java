package backend.academy.loganalyzer.template;

import java.util.Map;

/**
 * LogResult - класс для работы с данными LogAnalyzer и формирования финального отчета.
 */
public record LogResult(long totalRequests, double averageResponseSize, Map<String, Long> resourceCounts,
                        Map<Integer, Long> statusCodeCounts, double percentile) {
}
