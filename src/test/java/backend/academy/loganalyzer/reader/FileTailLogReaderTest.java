package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileTailLogReaderTest {

    private Path tempFile;
    private FileTailLogReader reader;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("tail-test", ".log");
        Files.write(tempFile, List.of("line1", "line2"));
        reader = new FileTailLogReader(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void readThrowsUnsupportedOnBatch() {
        assertThatThrownBy(reader::read)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("не поддерживает batch");
    }

    @Test
    void readConsumerThrowsWhenFileNotFound() {

        FileTailLogReader bad = new FileTailLogReader("no-such-file.log");

        assertThatThrownBy(() -> bad.read(line -> {
        }))
            .isInstanceOf(IOException.class);
    }

    @Test
    void readConsumerAppendsAndStopsOnInterrupt() throws Exception {

        List<String> consumed = Collections.synchronizedList(new ArrayList<>());
        Consumer<String> consumer = consumed::add;

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<?> future = exec.submit(() -> {
            try {
                reader.read(consumer);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Thread.sleep(200);

        Files.writeString(tempFile, "\nline3\nline4", StandardOpenOption.APPEND);
        Thread.sleep(500);

        future.cancel(true);
        exec.shutdownNow();
        exec.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(consumed)
            .containsSubsequence("line1", "line2", "line3", "line4");
    }
}
