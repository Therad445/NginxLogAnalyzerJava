package io.github.therad445.loganalyzer.util;

import io.github.therad445.loganalyzer.alert.AlertManager;
import io.github.therad445.loganalyzer.analyzer.SuspiciousIpDetector;
import io.github.therad445.loganalyzer.anomaly.Anomaly;
import io.github.therad445.loganalyzer.anomaly.AnomalyConfigurator;
import io.github.therad445.loganalyzer.anomaly.AnomalyService;
import io.github.therad445.loganalyzer.anomaly.MetricSnapshot;
import io.github.therad445.loganalyzer.anomaly.MetricsAggregator;
import io.github.therad445.loganalyzer.config.Config;
import io.github.therad445.loganalyzer.model.LogRecord;
import io.github.therad445.loganalyzer.parser.NginxLogParser;
import io.github.therad445.loganalyzer.reader.Reader;
import io.github.therad445.loganalyzer.visual.ChartGenerator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import static io.github.therad445.loganalyzer.alert.buildAlertManager.buildAlertManager;

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
