package backend.academy.loganalyzer.util;

import backend.academy.loganalyzer.alert.AlertManager;
import backend.academy.loganalyzer.analyzer.SuspiciousIpDetector;
import backend.academy.loganalyzer.anomaly.Anomaly;
import backend.academy.loganalyzer.anomaly.AnomalyConfigurator;
import backend.academy.loganalyzer.anomaly.AnomalyService;
import backend.academy.loganalyzer.anomaly.MetricSnapshot;
import backend.academy.loganalyzer.anomaly.MetricsAggregator;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.model.LogRecord;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.visual.ChartGenerator;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static backend.academy.loganalyzer.alert.buildAlertManager.buildAlertManager;

@Slf4j
public class RunStreaming {
    public static void runStreaming(Reader reader) {
        NginxLogParser parser = new NginxLogParser();
        MetricsAggregator aggregator = new MetricsAggregator(
            Config.aggregationWindow(), 5
        );
        AnomalyService anomalySvc = AnomalyConfigurator.defaultService();
        SuspiciousIpDetector ipDetector =
            new SuspiciousIpDetector(Config.aggregationWindow(), 10);
        AlertManager alert = buildAlertManager();
        ChartGenerator chartGen = new ChartGenerator();

        try {
            reader.read(line -> {
                try {

                    LogRecord record = parser.parse(Stream.of(line)).get(0);

                    List<MetricSnapshot> snaps = aggregator.addAndAggregate(record);
                    Map<String, List<Anomaly>> anomalies = anomalySvc.detectAll(snaps);

                    Set<String> suspiciousIps = ipDetector.detect(List.of(record));

                    if (!anomalies.isEmpty() || !suspiciousIps.isEmpty()) {
                        StringBuilder msg = new StringBuilder("*Аномалии на потоке:*\n");
                        anomalies.forEach((m, list) ->
                            msg.append("• ").append(m).append(": ").append(list.size()).append("\n"));
                        suspiciousIps.forEach(ip ->
                            msg.append("🚨 ").append(ip).append("\n"));
                        alert.send(msg.toString());
                    }

                    if (aggregator.shouldEmitChart()) {
                        String path = "output/stream_chart.png";
                        chartGen.generateTimeSeriesChart(snaps, path);
                        alert.sendImage(new File(path), "📊 Стриминговый график");
                    }

                } catch (Exception ex) {
                    log.error("Ошибка в стриминге", ex);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
