package backend.academy.loganalyzer.template;

import backend.academy.loganalyzer.anomaly.Anomaly;
import java.util.List;
import java.util.Map;

/**
 * LogResult - класс для работы с данными LogAnalyzer и формирования финального отчета.
 */
public record LogResult(
    long totalRequests,
    double averageResponseSize,
    Map<String, Long> resourceCounts,
    Map<Integer, Long> statusCodeCounts,
    double percentile,
    Map<String, List<Anomaly>> anomalies   // ← новое поле
) {
    public LogResult {
        if (totalRequests < 0) throw new IllegalArgumentException("totalRequests < 0");
        if (averageResponseSize < 0) throw new IllegalArgumentException("averageResponseSize < 0");
        if (resourceCounts == null) throw new NullPointerException("resourceCounts null");
        if (statusCodeCounts == null) throw new NullPointerException("statusCodeCounts null");
        if (percentile < 0) throw new IllegalArgumentException("percentile < 0");
        if (anomalies == null) anomalies = Map.of();   // допускаем «нет аномалий»
    }
}
