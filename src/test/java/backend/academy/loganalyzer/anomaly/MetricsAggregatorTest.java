package backend.academy.loganalyzer.anomaly;

import backend.academy.loganalyzer.model.LogRecord;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static backend.academy.loganalyzer.model.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsAggregatorTest {

    private MetricsAggregator aggregator;

    @BeforeEach
    void setup() {
        aggregator = new MetricsAggregator(Duration.ofMinutes(1), 3);
    }

    @Test
    void aggregatesRecordsIntoOneMinuteBuckets() {
        List<LogRecord> logs = List.of(
            log("00:00:01"), log("00:00:15"), log("00:00:55"),
            log("00:01:02"), log("00:01:45"),
            log("00:02:10")
        );
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
        List<MetricSnapshot> result = aggregator.aggregate(logs);

        assertEquals(2, result.get(0).errors());
        assertEquals(1, result.get(1).errors());
    }

    @Test
    void addAndAggregate_accumulatesAndTransitionsWindow() {
        assertTrue(aggregator.addAndAggregate(log("00:00:10")).isEmpty());
        assertTrue(aggregator.addAndAggregate(log("00:00:40")).isEmpty());
        List<MetricSnapshot> history = aggregator.addAndAggregate(log("00:01:01"));
        assertEquals(1, history.size());
        assertEquals(2, history.get(0).requests());
    }

    @Test
    void shouldEmitChart_returnsTrueEveryNthWindow() {
        aggregator.addAndAggregate(log("00:00:01"));
        aggregator.addAndAggregate(log("00:01:00"));
        aggregator.addAndAggregate(log("00:02:00"));
        aggregator.addAndAggregate(log("00:03:00"));
        aggregator.addAndAggregate(log("00:04:00"));

        assertTrue(aggregator.shouldEmitChart());
        assertFalse(aggregator.shouldEmitChart());
        aggregator.addAndAggregate(log("00:05:00"));
        aggregator.addAndAggregate(log("00:06:00"));
        aggregator.addAndAggregate(log("00:07:00"));
        assertTrue(aggregator.shouldEmitChart());
    }

    @Test
    void aggregate_returnsEmptyListForEmptyInput() {
        assertTrue(new MetricsAggregator(Duration.ofSeconds(10), 5)
            .aggregate(List.of())
            .isEmpty());
    }

    @Test
    void aggregatesToCorrectWindowEnds() {
        List<LogRecord> logs = List.of(
            log("00:00:02"),
            log("00:01:01"),
            log("00:01:59")
        );
        List<MetricSnapshot> result = aggregator.aggregate(logs);
        assertEquals(2, result.size());

        assertEquals(2, result.get(1).requests());
    }

    private LogRecord log(String time) {
        return log(time, 200);
    }

    private LogRecord log(String time, int status) {
        LocalDateTime ldt = LocalDateTime.parse("2024-01-01T" + time);
        return new LogRecord(
            "127.0.0.1",
            "-",
            ldt,
            GET,
            "/test",
            status,
            1000L,
            "-",
            "Mozilla"
        );
    }
}
