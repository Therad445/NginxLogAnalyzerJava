package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileReader implements Reader {
    @Override
    public Stream<String> read(String filePathFile) throws IOException {
        return Files.lines(Path.of(filePathFile));
    }
}
