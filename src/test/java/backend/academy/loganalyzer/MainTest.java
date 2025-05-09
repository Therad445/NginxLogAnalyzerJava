package backend.academy.loganalyzer;

import backend.academy.loganalyzer.reader.Reader;
import backend.academy.loganalyzer.reader.ReaderSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class MainTest {

    @Test
    void main_batchMode_runsSuccessfully(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.writeString(logFile, """
            127.0.0.1 - - [10/May/2024:13:55:36 +0000] "GET /index.html HTTP/1.1" 200 512 "-" "Mozilla"
            127.0.0.1 - - [10/May/2024:13:55:37 +0000] "GET /404 HTTP/1.1" 404 128 "-" "Mozilla"
        """);

        Reader mockReader = mock(Reader.class);
        when(mockReader.read()).thenReturn(Stream.of(
            "127.0.0.1 - - [10/May/2024:13:55:36 +0000] \"GET /index.html HTTP/1.1\" 200 512 \"-\" \"Mozilla\"",
            "127.0.0.1 - - [10/May/2024:13:55:37 +0000] \"GET /404 HTTP/1.1\" 404 128 \"-\" \"Mozilla\""
        ));

        try (MockedStatic<ReaderSelector> selector = mockStatic(ReaderSelector.class)) {
            selector.when(() -> ReaderSelector.select(any())).thenReturn(mockReader);

            String[] args = {
                "--source", "file",
                "--path", logFile.toString(),
                "--format", "markdown"
            };

            Main.main(args);
        }
    }

    @Test
    void main_streamingMode_runsSuccessfully() throws IOException {
        Reader mockReader = mock(Reader.class);
        doAnswer(invocation -> {
            var consumer = invocation.getArgument(0, java.util.function.Consumer.class);
            consumer.accept("127.0.0.1 - - [10/May/2024:13:55:36 +0000] \"GET /index.html HTTP/1.1\" 200 512 \"-\" \"Mozilla\"");
            return null;
        }).when(mockReader).read(any());

        try (MockedStatic<ReaderSelector> selector = mockStatic(ReaderSelector.class)) {
            selector.when(() -> ReaderSelector.select(any())).thenReturn(mockReader);

            String[] args = {
                "--source", "file",
                "--path", "fake-path",
                "--format", "json",
                "--stream"
            };

            Main.main(args);
        }
    }

    @Test
    void main_emptyLogFile_warnsAndExits(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("empty.log");
        Files.writeString(logFile, "");

        Reader mockReader = mock(Reader.class);
        when(mockReader.read()).thenReturn(Stream.empty());

        try (MockedStatic<ReaderSelector> selector = mockStatic(ReaderSelector.class)) {
            selector.when(() -> ReaderSelector.select(any())).thenReturn(mockReader);

            String[] args = {
                "--source", "file",
                "--path", logFile.toString(),
                "--format", "json"
            };

            Main.main(args);
        }
    }
}
