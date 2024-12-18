package backend.academy.loganalyzer.reader;

import backend.academy.loganalyzer.template.LogResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ReaderSelectorTest {

    private static final String GOOGLE_COM = "https://www.google.com";
    private static final String NODIRECT = "nodirect";
    private static final String NGINX_LOGS_TXT = "nginx_logs.txt";

    @Test
    void typeSelector_IsURL() throws IOException {
        //Arrange
        String path = GOOGLE_COM;
        Reader readerValid = new URLReader();
        //Act
        Reader readerSelected = ReaderSelector.typeSelector(path);
        //Assert
        assertEquals(readerValid.getClass(), readerSelected.getClass());
    }

    @Test
    void typeSelector_IsPath() throws IOException {
        //Arrange
        String path = NGINX_LOGS_TXT;
        Reader readerValid = new FileReader();
        //Act
        Reader readerSelected = ReaderSelector.typeSelector(path);
        //Assert
        assertEquals(readerValid.getClass(), readerSelected.getClass());
    }

    @Test
    void typeSelector_IsInvalid() throws IOException {
        //Arrange
        String path = "!@!#";
        Reader readerValid = new FileReader();
        //Act
        Exception exception = assertThrows(NoSuchFileException.class, () -> ReaderSelector.typeSelector(path));
        //Assert
        assertEquals("Нет файла по данному пути: " + Path.of(path).toAbsolutePath(), exception.getMessage());
    }

    @Test
    void isUrl_IsValid() {
        //Arrange
        String url = GOOGLE_COM;
        //Act
        boolean result = ReaderSelector.isUrl(url);
        //Assert
        assertTrue(result);
    }

    @Test
    void isUrl_IsInvalid() {
        //Arrange
        String url = GOOGLE_COM;
        //Act
        boolean result = ReaderSelector.isUrl(url);
        //Assert
        assertTrue(result);
    }

    @Test
    void checkPath_IsValid() throws NoSuchFileException {
        //Arrange
        String path = NGINX_LOGS_TXT;
        //Act
        boolean result = ReaderSelector.checkPath(path);
        //Assert
        assertTrue(result);
    }

    @Test
    void checkPath_IsValidAbsolutePath() throws NoSuchFileException {
        //Arrange
        String path = NGINX_LOGS_TXT;
        Path filePath = Path.of(path);
        String absolutePath = filePath.toAbsolutePath().toString();
        //Act
        boolean result = ReaderSelector.checkPath(absolutePath);
        //Assert
        assertTrue(result);
    }

    @Test
    void checkPath_IsInvalid() throws NoSuchFileException {
        //Arrange
        String path = NODIRECT;
        //Act
        Exception exception = assertThrows(NoSuchFileException.class, () -> ReaderSelector.checkPath(path));
        //Assert
        assertEquals("Нет файла по данному пути: " + Path.of(path).toAbsolutePath(), exception.getMessage());
    }

    @Test
    void checkPath_IsInvalidAbsolutePath() throws NoSuchFileException {
        //Arrange
        String path = NODIRECT;
        Path filePath = Path.of(path);
        String absolutePath = filePath.toAbsolutePath().toString();
        //Act
        Exception exception = assertThrows(NoSuchFileException.class, () -> ReaderSelector.checkPath(path));
        //Assert
        assertEquals("Нет файла по данному пути: " + Path.of(path).toAbsolutePath(), exception.getMessage());
    }

    @Test
    void isUrl_WithoutScheme() {
        assertFalse(ReaderSelector.isUrl("www.google.com"));
    }

    @Test
    void isUrl_WithSubdomain() {
        assertTrue(ReaderSelector.isUrl("https://sub.example.com"));
    }

    @Test
    void checkPath_NullValue() {
        Exception exception = assertThrows(NullPointerException.class,
            () -> ReaderSelector.checkPath(null));
    }

}
