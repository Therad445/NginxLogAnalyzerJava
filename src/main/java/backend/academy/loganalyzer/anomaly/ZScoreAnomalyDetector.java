package backend.academy.loganalyzer.anomaly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ZScoreAnomalyDetector implements AnomalyDetector {
    private final Function<MetricSnapshot, Double> metricExtractor;
    private final int windowSize;
    private final double zThreshold;
    private final String metricName;

    public ZScoreAnomalyDetector(
        String metricName,
        Function<MetricSnapshot, Double> metricExtractor,
        int windowSize,
        double zThreshold
    ) {
        this.metricName = metricName;
        this.metricExtractor = metricExtractor;
        this.windowSize = windowSize;
        this.zThreshold = zThreshold;
    }

    @Override
    public List<Anomaly> detect(List<MetricSnapshot> history) {
        if (history.size() < windowSize + 1) {
            return List.of();
        }
        List<Anomaly> anomalies = new ArrayList<>();

        for (int i = windowSize; i < history.size(); i++) {
            double[] window = history.subList(i - windowSize, i).stream()
                .mapToDouble(metricExtractor::apply)
                .toArray();
            double mean = Arrays.stream(window).average().orElse(0);
            double std = Math.sqrt(Arrays.stream(window).map(x -> Math.pow(x - mean, 2)).sum() / window.length);
            double x = metricExtractor.apply(history.get(i));

//            log.debug("Z-Score calc → mean={}, std={}, x={} (metric: {})", mean, std, x, metricName);

            if (std == 0) {
                if (x != mean) {
                    anomalies.add(
                        new Anomaly(history.get(i).timestamp(), metricName, x, mean, Double.POSITIVE_INFINITY));
                    log.warn("⚠ Anomaly detected (zero std): {} → x = {}, mean = {}", metricName, x, mean);
                }
                continue;
            }
            double z = Math.abs((x - mean) / std);
//            log.debug("→ z = {} (threshold = {})", z, zThreshold);

            if (z >= zThreshold) {
                anomalies.add(new Anomaly(history.get(i).timestamp(), metricName, x, mean + zThreshold * std, z));
                log.warn("⚠ Anomaly detected: {} (z = {})", metricName, z);
            }
        }
        return anomalies;
    }
}
