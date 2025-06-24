package io.github.therad445.loganalyzer.anomaly;

import io.github.therad445.loganalyzer.config.Config;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyConfiguratorTest {

    @Test
    void defaultService_hasFourDetectorsInCorrectOrder() {
        AnomalyService service = AnomalyConfigurator.defaultService();
        List<AnomalyDetector> detectors = service.getDetectors();

        assertThat(detectors)
            .as("Ожидаем 4 детектора, в порядке: Z, E, Z, E")
            .hasSize(4)
            .extracting(d -> d.getClass().getSimpleName())
            .containsExactly(
                "ZScoreAnomalyDetector",
                "EwmaAnomalyDetector",
                "ZScoreAnomalyDetector",
                "EwmaAnomalyDetector"
            );
    }

    @Test
    void defaultService_configuresZScoreDetectorsCorrectly() throws Exception {
        int expectedWindow = (int) Config.aggregationWindow().getSeconds();
        double expectedZ = Config.zThreshold();

        AnomalyService service = AnomalyConfigurator.defaultService();
        ZScoreAnomalyDetector z1 = (ZScoreAnomalyDetector) service.getDetectors().get(0);
        ZScoreAnomalyDetector z2 = (ZScoreAnomalyDetector) service.getDetectors().get(2);

        assertThat((String) getPrivateField(z1, "metricName")).isEqualTo("reqsPerWindow");
        assertThat((Integer) getPrivateField(z1, "windowSize")).isEqualTo(expectedWindow);
        assertThat((Double) getPrivateField(z1, "zThreshold")).isEqualTo(expectedZ);

        assertThat((String) getPrivateField(z2, "metricName")).isEqualTo("errorRate");
        assertThat((Integer) getPrivateField(z2, "windowSize")).isEqualTo(expectedWindow);
        assertThat((Double) getPrivateField(z2, "zThreshold")).isEqualTo(expectedZ);

        MetricSnapshot sample = new MetricSnapshot(Instant.now(), 10, 2, 100.0, 0.25, 10);
        assertThat(z1.metricFunction().apply(sample)).isEqualTo(10.0);
        assertThat(z2.metricFunction().apply(sample)).isEqualTo(0.25);
    }

    @Test
    void defaultService_configuresEwmaDetectorsCorrectly() throws Exception {
        double expectedAlpha = 0.3;
        double expectedK = 3.0;

        AnomalyService service = AnomalyConfigurator.defaultService();
        EwmaAnomalyDetector e1 = (EwmaAnomalyDetector) service.getDetectors().get(1);
        EwmaAnomalyDetector e2 = (EwmaAnomalyDetector) service.getDetectors().get(3);

        assertThat((String) getPrivateField(e1, "metricName")).isEqualTo("reqsPerWindow");
        assertThat((Double) getPrivateField(e1, "alpha")).isEqualTo(expectedAlpha);
        assertThat((Double) getPrivateField(e1, "k")).isEqualTo(expectedK);

        assertThat((String) getPrivateField(e2, "metricName")).isEqualTo("errorRate");
        assertThat((Double) getPrivateField(e2, "alpha")).isEqualTo(expectedAlpha);
        assertThat((Double) getPrivateField(e2, "k")).isEqualTo(expectedK);

        MetricSnapshot sample = new MetricSnapshot(Instant.now(), 8, 1, 50.0, 0.15, 8);
        assertThat(e1.metricFunction().apply(sample)).isEqualTo(8.0);
        assertThat(e2.metricFunction().apply(sample)).isEqualTo(0.15);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }
}
