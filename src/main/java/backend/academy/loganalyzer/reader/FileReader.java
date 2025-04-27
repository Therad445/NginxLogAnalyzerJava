package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileReader implements Reader {
    private final Path path;

    public FileReader(String path) {
        this.path = Path.of(path);
    }

    @Override
    public Stream<String> read() throws IOException {
        return Files.lines(path);
    }

}
