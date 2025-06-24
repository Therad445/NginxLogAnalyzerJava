package io.github.therad445.loganalyzer.alert;

import java.io.File;

public class buildAlertManager {
    public static AlertManager buildAlertManager() {
        String token = System.getenv("TG_TOKEN");
        String chat = System.getenv("TG_CHAT");
        if (token != null && chat != null && !token.isBlank() && !chat.isBlank()) {
            return new TelegramAlertManager(token, chat);
        }
        return new AlertManager() {
            @Override public void send(String text) {
            }

            @Override public void sendImage(File image, String caption) {
            }
        };
    }
}
