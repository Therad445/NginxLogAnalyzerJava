package backend.academy.loganalyzer.anomaly;

import java.time.Instant;
import java.util.Objects;

/**
 * Снимок метрик за одно окно.
 *
 * @param timestamp         конец окна (Instant)
 * @param requests          общее число запросов
 * @param errors            число ошибок (status >= 400)
 * @param meanLatencyMillis средняя задержка в мс
 * @param errorRate         доля ошибок = errors/requests
 * @param reqsPerWindow     число запросов в окне (аналог reqs/min)
 */
public record MetricSnapshot(
    Instant timestamp,
    long requests,
    long errors,
    double meanLatencyMillis,
    double errorRate,
    long reqsPerWindow
) {
    public MetricSnapshot {
        Objects.requireNonNull(timestamp);
        // при requests == 0 errorRate = 0
        if (requests == 0) {
            errorRate = 0.0;
        }
    }
}
