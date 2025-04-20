package backend.academy.loganalyzer.anomaly;

import java.util.List;

public class AnomalyConfigurator {
    public static AnomalyService defaultService() {
        AnomalyDetector reqSpike = new ZScoreAnomalyDetector(
            "reqs/min",
            ms -> (double) ms.requests(),
            30,
            3.0
        );

        AnomalyDetector errorRate =
            new EwmaAnomalyDetector("errorRate", ms -> ms.requests() == 0 ? 0 : (double) ms.errors() / ms.requests(),
                0.3, 3.0);
        return new AnomalyService(List.of(reqSpike, errorRate));
    }
}
