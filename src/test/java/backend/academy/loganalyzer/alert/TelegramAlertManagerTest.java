package backend.academy.loganalyzer.alert;

import java.io.File;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramAlertManagerTest {

    private OkHttpClient mockClient;
    private TelegramAlertManager manager;

    @BeforeEach
    void setup() {
        mockClient = mock(OkHttpClient.class);
        manager = new TelegramAlertManager("dummy-token", "12345") {
            @Override
            protected OkHttpClient client() {
                return mockClient;
            }
        };
    }

    @Test
    void send_shouldEscapeMarkdown_andSendRequest() throws IOException {
        String text = "*bold* text with _symbols_";
        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("https://api.telegram.org").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200).message("OK")
            .body(ResponseBody.create("ok", MediaType.parse("application/json")))
            .build();

        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        manager.send(text);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockClient).newCall(requestCaptor.capture());
        Request sentRequest = requestCaptor.getValue();

        assertTrue(sentRequest.url().toString().contains("sendMessage"));
        assertEquals("POST", sentRequest.method());

        RequestBody body = sentRequest.body();
        assertTrue(body instanceof FormBody);
        FormBody form = (FormBody) body;

        boolean hasMarkdownV2 = false;
        for (int i = 0; i < form.size(); i++) {
            if (form.name(i).equals("parse_mode") && form.value(i).equals("MarkdownV2")) {
                hasMarkdownV2 = true;
                break;
            }
        }

        assertTrue(hasMarkdownV2, "parse_mode should be MarkdownV2");
    }


    @Test
    void sendImage_shouldPostMultipartRequest() throws IOException {
        File dummyFile = File.createTempFile("test-image", ".png");
        dummyFile.deleteOnExit();

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("https://api.telegram.org").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200).message("OK")
            .body(ResponseBody.create("ok", MediaType.parse("application/json")))
            .build();

        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        TelegramAlertManager mgr = new TelegramAlertManager("dummy-token", "12345") {
            @Override
            protected OkHttpClient client() {
                return mockClient;
            }
        };

        mgr.sendImage(dummyFile, "Test Caption");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockClient).newCall(requestCaptor.capture());
        Request sentRequest = requestCaptor.getValue();

        assertTrue(sentRequest.url().toString().contains("sendPhoto"));
        assertEquals("POST", sentRequest.method());
        assertTrue(sentRequest.body().contentType().toString().startsWith("multipart/form-data"));
    }
}
