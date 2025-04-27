package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Reader {
    Stream<String> read() throws IOException;

    default void read(Consumer<String> consumer) throws IOException {
        read().forEach(consumer);
    }
}
