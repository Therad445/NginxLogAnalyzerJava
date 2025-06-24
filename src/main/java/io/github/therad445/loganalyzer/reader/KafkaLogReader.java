package io.github.therad445.loganalyzer.reader;

import io.github.therad445.loganalyzer.config.Config;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaLogReader implements Reader {

    private final org.apache.kafka.clients.consumer.Consumer<String, String> consumer;

    public KafkaLogReader(Config cfg) {
        Properties props = new Properties();
        props.put("bootstrap.servers", cfg.kafkaBootstrapServers());
        props.put("group.id", cfg.kafkaGroupId());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(cfg.kafkaTopic()));
    }

    KafkaLogReader(org.apache.kafka.clients.consumer.Consumer<String, String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Stream<String> read() {
        throw new UnsupportedOperationException("KafkaLogReader не поддерживает batch");
    }

    @Override
    public void read(Consumer<String> consumerFn) {
        while (true) {
            var recs = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> rec : recs) {
                consumerFn.accept(rec.value());
            }
        }
    }
}
