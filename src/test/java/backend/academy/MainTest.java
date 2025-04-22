package backend.academy;

import backend.academy.loganalyzer.alert.AlertManager;
import backend.academy.loganalyzer.config.Config;
import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void buildAlertManager_returnsNoopIfEnvMissing() {
        assertDoesNotThrow(() -> {
            var method = Main.class.getDeclaredMethod("buildAlertManager");
            method.setAccessible(true);
            AlertManager alertManager = (AlertManager) method.invoke(null); // ✅ Правильный тип
            alertManager.send("Test message"); // вызов интерфейсного метода
        });
    }


    @Test
    void parsesArgumentsIntoConfig() {
        String[] args = {"--path", "file.log", "--format", "markdown"};
        Config config = new Config();
        JCommander.newBuilder().addObject(config).build().parse(args);

        assertEquals("file.log", config.path());
        assertEquals("markdown", config.format());
    }

    @Test
    void getLogResult_returnsNullIfInvalidPath() throws Exception {
        var method = Main.class.getDeclaredMethod("getLogResult",
            Config.class, String.class, String.class, String.class, String.class, String.class);

        method.setAccessible(true);
        Config dummy = new Config(); // пустой конфиг
        Object result = method.invoke(null, dummy, "bad_path", null, null, null, null);
        assertNull(result);
    }

}
