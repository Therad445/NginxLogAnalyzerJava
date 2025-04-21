package backend.academy.loganalyzer.alert;

import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Log4j2
public class TelegramAlertManager implements AlertManager {

    protected final OkHttpClient client = new OkHttpClient();

    private final String token;
    private final String chatId;

    public TelegramAlertManager(String token, String chatId) {
        this.token = token;
        this.chatId = chatId;
    }

    @Override
    public void send(String text) {
        String markdownText = text
            .replace("_", "\\_")   // экранируем спецсимволы
            .replace("*", "\\*")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("=", "\\=")
            .replace("|", "\\|")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace(".", "\\.")
            .replace("!", "\\!");

        RequestBody body = new FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", markdownText)
            .add("parse_mode", "MarkdownV2")

            .build();

        Request req = new Request.Builder()
            .url("https://api.telegram.org/bot" + token + "/sendMessage")
            .post(body)
            .build();

        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                log.error("❌ Telegram API error: {} → {}", response.code(), response.body().string());
            }
        } catch (IOException e) {
            log.error("❌ Telegram alert failed: {}", e.getMessage());
        }
    }

}
