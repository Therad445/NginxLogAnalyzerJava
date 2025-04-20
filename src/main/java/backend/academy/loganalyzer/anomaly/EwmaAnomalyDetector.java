package backend.academy.loganalyzer.anomaly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EwmaAnomalyDetector implements AnomalyDetector {
    private final Function<MetricSnapshot, Double> metricExtractor;
    private final double alpha;
    private final double k;
    private final String metricName;

    public EwmaAnomalyDetector(
        String metricName,
        Function<MetricSnapshot, Double> metricExtractor,
        double alpha,
        double k
    ) {
        this.metricName = metricName;
        this.metricExtractor = metricExtractor;
        this.alpha = alpha;
        this.k = k;
    }

    @Override public List<Anomaly> detect(List<MetricSnapshot> history) {
        if (history.isEmpty()) {
            return List.of();
        }
        List<Anomaly> res = new ArrayList<>();
        double mu = metricExtractor.apply(history.get(0));
        double var = 0;
        for (int i = 1; i < history.size(); i++) {
            double x = metricExtractor.apply(history.get(i));
            mu = alpha * x + (1 - alpha) * mu;
            var = alpha * Math.pow(x - mu, 2) + (1 - alpha) * var;
            double sigma = Math.sqrt(var);
            if (sigma == 0) {
                continue;
            }
            if (x > mu + k * sigma) {
                res.add(new Anomaly(history.get(i).timestamp(), metricName, x, mu + k * sigma, (x - mu) / sigma));
            }
        }
        return res;
    }
}
