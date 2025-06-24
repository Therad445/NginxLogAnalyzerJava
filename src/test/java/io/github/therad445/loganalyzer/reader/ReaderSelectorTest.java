package io.github.therad445.loganalyzer.reader;

import io.github.therad445.loganalyzer.config.Config;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReaderSelectorTest {

    @Test
    void selectFileInBatchMode() throws IOException {
        Config cfg = new Config();
        cfg.source("file");
        cfg.streamingMode(false);
        cfg.path("some/path.log");

        Reader r = ReaderSelector.select(cfg);
        assertThat(r).isInstanceOf(FileReader.class);
    }

    @Test
    void selectFileInStreamMode() throws IOException {
        Config cfg = new Config();
        cfg.source("file");
        cfg.streamingMode(true);
        cfg.path("some/path.log");

        Reader r = ReaderSelector.select(cfg);
        assertThat(r).isInstanceOf(FileTailLogReader.class);
    }

    @Test
    void selectUrlInBatchMode() throws IOException {
        Config cfg = new Config();
        cfg.source("url");
        cfg.streamingMode(false);
        cfg.path("http://example.com/log");

        Reader r = ReaderSelector.select(cfg);
        assertThat(r).isInstanceOf(URLReader.class);
    }

    @Test
    void selectUrlInStreamModeThrows() {
        Config cfg = new Config();
        cfg.source("url");
        cfg.streamingMode(true);
        cfg.path("http://example.com/log");

        assertThatThrownBy(() -> ReaderSelector.select(cfg))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("URL не поддерживает --stream");
    }

    @Test
    void selectKafkaInStreamMode() throws IOException {
        Config cfg = new Config();
        cfg.source("kafka");
        cfg.streamingMode(true);
        cfg.kafkaBootstrapServers("localhost:9092");
        cfg.kafkaTopic("topic");
        cfg.kafkaGroupId("gid");

        Reader r = ReaderSelector.select(cfg);
        assertThat(r).isInstanceOf(KafkaLogReader.class);
    }

    @Test
    void selectKafkaInBatchModeThrows() {
        Config cfg = new Config();
        cfg.source("kafka");
        cfg.streamingMode(false);

        assertThatThrownBy(() -> ReaderSelector.select(cfg))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Kafka только в streaming-режиме");
    }

    @Test
    void selectUnknownSourceThrows() {
        Config cfg = new Config();
        cfg.source("unknown");
        cfg.streamingMode(false);
        cfg.path("foo");

        assertThatThrownBy(() -> ReaderSelector.select(cfg))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown source");
    }
}
