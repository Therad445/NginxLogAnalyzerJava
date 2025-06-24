package io.github.therad445.loganalyzer.model;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogRecordTest {

    @Test
    void recordShouldStoreAllFieldsCorrectly() {
        LocalDateTime time = LocalDateTime.of(2025, 5, 9, 18, 30);
        LogRecord record = new LogRecord(
            "192.168.0.1",
            "admin",
            time,
            HttpMethod.GET,
            "/index.html",
            200,
            1024L,
            "https://ref.example.com",
            "Mozilla/5.0"
        );

        assertEquals("192.168.0.1", record.remoteAddr());
        assertEquals("admin", record.remoteUser());
        assertEquals(time, record.timestamp());
        assertEquals(HttpMethod.GET, record.method());
        assertEquals("/index.html", record.request());
        assertEquals(200, record.status());
        assertEquals(1024L, record.bodyBytesSent());
        assertEquals("https://ref.example.com", record.httpReferer());
        assertEquals("Mozilla/5.0", record.userAgent());
    }

    @Test
    void toString_shouldContainAllFieldValues() {
        LocalDateTime time = LocalDateTime.of(2025, 5, 9, 18, 30);
        LogRecord record = new LogRecord(
            "127.0.0.1", "user", time, HttpMethod.POST,
            "/submit", 201, 2048L, "http://example.com", "curl/7.81.0"
        );

        String str = record.toString();
        assertTrue(str.contains("127.0.0.1"));
        assertTrue(str.contains("user"));
        assertTrue(str.contains("2025-05-09T18:30"));
        assertTrue(str.contains("POST"));
        assertTrue(str.contains("/submit"));
        assertTrue(str.contains("201"));
        assertTrue(str.contains("2048"));
        assertTrue(str.contains("http://example.com"));
        assertTrue(str.contains("curl/7.81.0"));
    }
}
