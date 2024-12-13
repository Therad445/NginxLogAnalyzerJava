package backend.academy.loganalyzer.reader;

import lombok.experimental.UtilityClass;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@UtilityClass
public class ReaderSelector {
    public static Reader typeSelector(String path) throws IOException {
        if (isUrl(path)) {
            return new URLReader();
        } else if (checkPath(path)) {
            return new FileReader();
        } else {
            throw new IllegalArgumentException("Ошибка с указанным путём");
        }
    }

    public static boolean isUrl(String input) {
        try {
            new URI(input).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkPath(String path) throws NoSuchFileException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Нет файла по данному пути: " + filePath.toAbsolutePath());
        }
        return true;
    }
}
