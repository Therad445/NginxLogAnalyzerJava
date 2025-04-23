package backend.academy.loganalyzer.visual;

import backend.academy.loganalyzer.anomaly.MetricSnapshot;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

public class ChartGenerator {

    public void generateTimeSeriesChart(List<MetricSnapshot> snapshots, String outputPath) throws IOException {
        List<Date> times = snapshots.stream()
            .map(s -> Date.from(s.timestamp()))
            .collect(Collectors.toList());
        List<Long> reqs = snapshots.stream().map(MetricSnapshot::requests).collect(Collectors.toList());
        List<Double> errs = snapshots.stream()
            .map(ms -> ms.requests() == 0 ? 0.0 : (double) ms.errors() / ms.requests())
            .collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
            .title("Трафик и уровень ошибок")
            .xAxisTitle("Время")
            .yAxisTitle("reqs/min / errorRate")
            .width(800).height(600)
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.addSeries("reqs/min", times, reqs);
        chart.addSeries("errorRate", times, errs);

        File file = new File(outputPath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        org.knowm.xchart.BitmapEncoder.saveBitmap(chart, outputPath, org.knowm.xchart.BitmapEncoder.BitmapFormat.PNG);
    }
}
