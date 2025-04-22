package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.config.Config;
import java.util.List;

public class AnomalyConfigurator {
    public static AnomalyService defaultService() {
        AnomalyDetector reqSpike = new ZScoreAnomalyDetector(
            "reqs/min",
            ms -> (double) ms.requests(),
            (int) (Config.aggregationWindow().getSeconds() / Config.aggregationWindow().getSeconds()),
            Config.zThreshold()
        );

        AnomalyDetector errorRate =
            new EwmaAnomalyDetector("errorRate", ms -> ms.requests() == 0 ? 0 : (double) ms.errors() / ms.requests(),
                0.3, 1.0);
        return new AnomalyService(List.of(reqSpike, errorRate));
    }
}
