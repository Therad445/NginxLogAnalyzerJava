package backend.academy.loganalyzer.reader;

import backend.academy.loganalyzer.config.Config;
import java.io.IOException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReaderSelector {
    public static Reader select(Config cfg) throws IOException {
        switch (cfg.source().toLowerCase()) {
            case "kafka":
                if (!cfg.streamingMode()) {
                    throw new IllegalArgumentException("Kafka только в streaming-режиме");
                }
                return new KafkaLogReader(cfg);

            case "file":
                return cfg.streamingMode()
                    ? new FileTailLogReader(cfg.path())
                    : new FileReader(cfg.path());

            case "url":
                if (cfg.streamingMode()) {
                    throw new IllegalArgumentException("URL не поддерживает --stream");
                }
                return new URLReader(cfg.path());

            default:
                throw new IllegalArgumentException("Unknown source: " + cfg.source());
        }
    }
}
