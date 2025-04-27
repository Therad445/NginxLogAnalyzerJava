package backend.academy;

import backend.academy.loganalyzer.alert.AlertManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void buildAlertManager_returnsNoopIfEnvMissing() {
        assertDoesNotThrow(() -> {
            var method = Main.class.getDeclaredMethod("buildAlertManager");
            method.setAccessible(true);
            AlertManager alertManager = (AlertManager) method.invoke(null);
            alertManager.send("Test message");
        });
    }
}
