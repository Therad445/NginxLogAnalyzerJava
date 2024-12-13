package backend.academy.loganalyzer.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LogPathSelector {
    public static Stream<String> typeSelector(String path) throws IOException {
        return getStringStream(path);
    }

    private static Stream<String> getStringStream(String path) throws IOException {
        if (isUrl(path)) {
            return readFromUrl(path);
        } else if (!path.isEmpty()) {
            return readFromFile(path);
        } else {
            throw new IllegalArgumentException("Путь пустой");
        }
    }

    public static void checkPath(String path) throws NoSuchFileException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Нет файла по пути: " + filePath.toAbsolutePath());
        }
    }

    private static Stream<String> readFromFile(String filePathFile) throws IOException {
        checkPath(filePathFile);
        return Files.lines(Path.of(filePathFile));
    }

    private static Stream<String> readFromUrl(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        return reader.lines();
    }

    public static boolean isUrl(String input) {
        try {
            new URI(input).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
