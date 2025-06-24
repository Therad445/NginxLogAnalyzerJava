package io.github.therad445.loganalyzer;

import io.github.therad445.loganalyzer.config.Config;
import io.github.therad445.loganalyzer.reader.Reader;
import io.github.therad445.loganalyzer.reader.ReaderSelector;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import static io.github.therad445.loganalyzer.util.RunBatch.runBatch;
import static io.github.therad445.loganalyzer.util.RunStreaming.runStreaming;

@Log4j2
@UtilityClass
public class Main {

    public static void main(String[] args) throws IOException {

        Config config = new Config();
        JCommander.newBuilder()
            .addObject(config)
            .build()
            .parse(args);

        Reader reader = ReaderSelector.select(config);

        if (config.streamingMode()) {
            runStreaming(reader);
        } else {
            runBatch(reader, config);
        }
    }
}
