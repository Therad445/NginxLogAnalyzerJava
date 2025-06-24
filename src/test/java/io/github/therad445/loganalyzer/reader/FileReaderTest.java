package io.github.therad445.loganalyzer.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileReaderTest {

    @Test
    void readExistingFileWithUtilityMethod() throws IOException {
        Path temp = Files.createTempFile("log", ".txt");
        List<String> lines = List.of("one", "two", "three");
        Files.write(temp, lines);

        FileReader fr = new FileReader(temp.toString());
        try (Stream<String> stream = fr.read()) {
            List<String> result = stream.toList();
            assertThat(result).containsExactlyElementsOf(lines);
        }
    }

    @Test
    void readExistingFileWithConstructor() throws IOException {
        Path temp = Files.createTempFile("log", ".txt");
        List<String> lines = List.of("α", "β");
        Files.write(temp, lines);

        FileReader fr = new FileReader(temp.toString());
        try (Stream<String> stream = fr.read()) {
            List<String> result = stream.toList();
            assertThat(result).containsExactly("α", "β");
        }
    }

    @Test
    void readNonexistentThrowsIOException() {
        FileReader fr = new FileReader("no-such-file.log");
        assertThatThrownBy(fr::read)
            .isInstanceOf(IOException.class);
    }

    @Test
    void readWithoutPathThrowsException() {
        FileReader fr = new FileReader("");
        assertThatThrownBy(fr::read)
            .isInstanceOf(IOException.class);
    }
}
