package backend.academy.loganalyzer.alert;

import java.io.File;

public interface AlertManager {
    void send(String text);

    void sendImage(File imageFile, String caption);
}
