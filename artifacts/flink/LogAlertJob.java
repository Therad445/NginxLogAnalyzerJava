// MIT License
package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class LogAlertJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("nginx_errors")
                .setGroupId("flink")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> alerts = env
                .fromSource(source, WatermarkStrategy.noWatermarks(), "kafka")
                .flatMap(new Detector());

        alerts.sinkTo(JdbcSink.sink(
                "INSERT INTO alerts(log) VALUES (?)",
                (ps, s) -> ps.setString(1, s),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:clickhouse://clickhouse:8123/default")
                        .build()));

        env.execute("LogAlertJob");
    }

    static class Detector extends RichFlatMapFunction<String, String> {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        private static final double ALPHA = 2.0 / 21;
        private double mean = 0;
        private double var = 0;
        @Override
        public void flatMap(String value, Collector<String> out) throws Exception {
            JsonNode n = MAPPER.readTree(value);
            double t = n.get("request_time").asDouble();
            mean = ALPHA * t + (1 - ALPHA) * mean;
            var = ALPHA * Math.pow(t - mean, 2) + (1 - ALPHA) * var;
            double sd = Math.sqrt(var);
            if (t > mean + 2.37 * sd) {
                out.collect(value);
            }
        }
    }
}
