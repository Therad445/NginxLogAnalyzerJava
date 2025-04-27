package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.config.Config;
import java.util.List;

public class AnomalyConfigurator {

    public static AnomalyService defaultService() {
        int windowSize = (int) Config.aggregationWindow().getSeconds();
        double zThresh = Config.zThreshold();
        double alpha = 0.3;
        double k = 3.0;

        return new AnomalyService(List.of(
            new ZScoreAnomalyDetector(
                "reqsPerWindow",
                ms -> (double) ms.reqsPerWindow(),
                windowSize,
                zThresh
            ),
            new EwmaAnomalyDetector(
                "reqsPerWindow",
                ms -> (double) ms.reqsPerWindow(),
                alpha,
                k
            ),
            new ZScoreAnomalyDetector(
                "errorRate",
                MetricSnapshot::errorRate,
                windowSize,
                zThresh
            ),
            new EwmaAnomalyDetector(
                "errorRate",
                MetricSnapshot::errorRate,
                alpha,
                k
            )
        ));
    }

}
