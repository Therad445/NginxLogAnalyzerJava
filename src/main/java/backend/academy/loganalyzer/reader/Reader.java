package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.util.stream.Stream;

public interface Reader {
    Stream<String> read(String string) throws IOException;
}
