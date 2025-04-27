package backend.academy.loganalyzer.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaLogReaderTest {

    private static final String TOPIC = "test-topic";

    private MockConsumer<String, String> mockConsumer;
    private KafkaLogReader reader;

    @BeforeEach
    void setUp() {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

        mockConsumer.subscribe(Collections.singletonList(TOPIC));
        mockConsumer.rebalance(List.of(new TopicPartition(TOPIC, 0)));
        mockConsumer.updateBeginningOffsets(Map.of(new TopicPartition(TOPIC, 0), 0L));

        reader = new KafkaLogReader(mockConsumer);
    }

    @Test
    void read_throwsUnsupportedOperationOnBatchRequest() {
        assertThatThrownBy(reader::read)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("batch");
    }

    @Test
    void read_consumesAllPolledRecords() throws Exception {
        List<String> collected = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(2);
        Consumer<String> fn = s -> {
            collected.add(s);
            latch.countDown();
        };

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<?> future = pool.submit(() -> reader.read(fn));

        Thread.sleep(50);

        mockConsumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 0L, "k1", "v1"));
        mockConsumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 1L, "k2", "v2"));

        assertThat(latch.await(1, TimeUnit.SECONDS))
            .as("all records delivered to consumerFn within timeout")
            .isTrue();

        assertThat(collected).containsExactly("v1", "v2");

        mockConsumer.wakeup();
        future.cancel(true);
        pool.shutdownNow();
    }
}
