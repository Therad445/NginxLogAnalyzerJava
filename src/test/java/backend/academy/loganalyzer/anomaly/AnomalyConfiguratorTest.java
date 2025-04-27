package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.config.Config;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnomalyConfiguratorTest {

    @Test
    void defaultService_hasFourDetectors() {

        AnomalyService service = AnomalyConfigurator.defaultService();
        List<AnomalyDetector> detectors = service.getDetectors();

        assertThat((Iterable<?>) detectors)
            .as("Ожидаем ровно четыре детектора в сервисе по умолчанию")
            .hasSize(4);
    }

    @Test
    void defaultService_configuresZScoreDetectorsCorrectly() throws Exception {
        int expectedWindow = (int) Config.aggregationWindow().getSeconds();
        double expectedZ = Config.zThreshold();

        AnomalyService service = AnomalyConfigurator.defaultService();
        ZScoreAnomalyDetector z1 = (ZScoreAnomalyDetector) service.getDetectors().get(0);
        ZScoreAnomalyDetector z2 = (ZScoreAnomalyDetector) service.getDetectors().get(2);

        String metricName1 = getPrivateField(z1, "metricName");
        assertThat(metricName1)
            .as("Первый ZScoreAnomalyDetector должен отслеживать reqsPerWindow")
            .isEqualTo("reqsPerWindow");

        int windowSize1 = getPrivateField(z1, "windowSize");
        assertThat(windowSize1)
            .as("Первый ZScoreAnomalyDetector должен иметь windowSize из конфигурации")
            .isEqualTo(expectedWindow);

        double thresh1 = getPrivateField(z1, "zThreshold");
        assertThat(thresh1)
            .as("Первый ZScoreAnomalyDetector должен иметь zThreshold из конфигурации")
            .isEqualTo(expectedZ);

        String metricName2 = getPrivateField(z2, "metricName");
        assertThat(metricName2)
            .as("Третий ZScoreAnomalyDetector должен отслеживать errorRate")
            .isEqualTo("errorRate");

        int windowSize2 = getPrivateField(z2, "windowSize");
        assertThat(windowSize2)
            .as("Третий ZScoreAnomalyDetector должен иметь windowSize из конфигурации")
            .isEqualTo(expectedWindow);

        double thresh2 = getPrivateField(z2, "zThreshold");
        assertThat(thresh2)
            .as("Третий ZScoreAnomalyDetector должен иметь zThreshold из конфигурации")
            .isEqualTo(expectedZ);
    }

    @Test
    void defaultService_configuresEwmaDetectorsCorrectly() throws Exception {
        double expectedAlpha = 0.3;
        double expectedK = 3.0;

        AnomalyService service = AnomalyConfigurator.defaultService();
        EwmaAnomalyDetector e1 = (EwmaAnomalyDetector) service.getDetectors().get(1);
        EwmaAnomalyDetector e2 = (EwmaAnomalyDetector) service.getDetectors().get(3);

        String metricName1 = getPrivateField(e1, "metricName");
        assertThat(metricName1)
            .as("Второй EwmaAnomalyDetector должен отслеживать reqsPerWindow")
            .isEqualTo("reqsPerWindow");

        double alpha1 = getPrivateField(e1, "alpha");
        assertThat(alpha1)
            .as("Второй EwmaAnomalyDetector должен иметь alpha=0.3")
            .isEqualTo(expectedAlpha);

        double k1 = getPrivateField(e1, "k");
        assertThat(k1)
            .as("Второй EwmaAnomalyDetector должен иметь k=3.0")
            .isEqualTo(expectedK);

        String metricName2 = getPrivateField(e2, "metricName");
        assertThat(metricName2)
            .as("Четвёртый EwmaAnomalyDetector должен отслеживать errorRate")
            .isEqualTo("errorRate");

        double alpha2 = getPrivateField(e2, "alpha");
        assertThat(alpha2)
            .as("Четвёртый EwmaAnomalyDetector должен иметь alpha=0.3")
            .isEqualTo(expectedAlpha);

        double k2 = getPrivateField(e2, "k");
        assertThat(k2)
            .as("Четвёртый EwmaAnomalyDetector должен иметь k=3.0")
            .isEqualTo(expectedK);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }
}
