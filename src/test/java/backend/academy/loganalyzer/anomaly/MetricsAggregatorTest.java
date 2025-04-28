package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.model.LogRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsAggregatorTest {

    @Test
    void aggregatesRecordsIntoOneMinuteBuckets() {
        List<LogRecord> logs = List.of(
            log("00:00:01"), log("00:00:15"), log("00:00:55"),
            log("00:01:02"), log("00:01:45"),
            log("00:02:10")
        );
        MetricsAggregator aggregator = new MetricsAggregator(Duration.ofMinutes(1), 5);
        List<MetricSnapshot> result = aggregator.aggregate(logs);

        assertEquals(3, result.size());
        assertEquals(3, result.get(0).requests());
        assertEquals(2, result.get(1).requests());
        assertEquals(1, result.get(2).requests());
    }

    @Test
    void correctlyCountsErrors() {
        List<LogRecord> logs = List.of(
            log("00:00:01", 200), log("00:00:15", 500), log("00:00:55", 404),
            log("00:01:02", 502), log("00:01:45", 200)
        );
        MetricsAggregator aggregator = new MetricsAggregator(Duration.ofMinutes(1), 5);
        List<MetricSnapshot> result = aggregator.aggregate(logs);

        assertEquals(2, result.get(0).errors());
        assertEquals(1, result.get(1).errors());
    }

    private LogRecord log(String time) {
        return log(time, 200);
    }

    private LogRecord log(String time, int status) {
        LocalDateTime ldt = LocalDateTime.parse("2024-01-01T" + time);
        Instant timestamp = ldt.toInstant(ZoneOffset.UTC);
        LogRecord rec = new LogRecord();
        rec.timestamp(ldt);
        rec.status(status);
        return rec;
    }
}
