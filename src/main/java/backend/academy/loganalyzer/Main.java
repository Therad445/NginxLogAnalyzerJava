package backend.academy.loganalyzer;

import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import static backend.academy.loganalyzer.util.RunBatch.runBatch;
import static backend.academy.loganalyzer.util.RunStreaming.runStreaming;

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
