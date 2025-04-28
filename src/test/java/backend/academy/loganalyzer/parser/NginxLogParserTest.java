package backend.academy.loganalyzer.parser;

import backend.academy.loganalyzer.model.LogRecord;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static backend.academy.loganalyzer.model.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NginxLogParserTest {

    private NginxLogParser parser;

    @BeforeEach
    void setUp() {
        parser = new NginxLogParser();
    }

    @Test
    void testParse_withValidLogLines() {

        Stream<String> logLines = Stream.of(
            "192.168.1.1 - user1 [09/Dec/2024:12:34:56 +0000] \"GET /index.html HTTP/1.1\" 200 512 \"-\" \"Mozilla/5.0\"",
            "203.0.113.5 - user2 [09/Dec/2024:12:35:56 +0000] \"POST /submit HTTP/1.1\" 404 128 \"-\" \"Chrome/90.0\""
        );

        List<LogRecord> records = parser.parse(logLines);

        assertEquals(2, records.size());

        LogRecord record1 = records.getFirst();
        assertEquals("192.168.1.1", record1.remoteAddr());
        assertEquals("/index.html", record1.request());
        assertEquals(GET, record1.method());
        assertEquals(200, record1.status());
        assertEquals(512, record1.bodyBytesSent());
        assertEquals("Mozilla/5.0", record1.userAgent());

        LogRecord record2 = records.get(1);
        assertEquals("203.0.113.5", record2.remoteAddr());
        assertEquals("/submit", record2.request());
        assertEquals(POST, record2.method());
        assertEquals(404, record2.status());
        assertEquals(128, record2.bodyBytesSent());
        assertEquals("Chrome/90.0", record2.userAgent());
    }

    @Test
    void testParse_withInvalidLogLines() {

        Stream<String> logLines = Stream.of(
            "INVALID LOG ENTRY",
            "another bad line"
        );

        List<LogRecord> records = parser.parse(logLines);

        assertTrue(records.isEmpty());
    }

    @Test
    void testParse_withMixedLogLines() {

        Stream<String> logLines = Stream.of(
            "192.168.1.1 - user1 [09/Dec/2024:12:34:56 +0000] \"GET /index.html HTTP/1.1\" 200 512 \"-\" \"Mozilla/5.0\"",
            "INVALID LOG ENTRY"
        );

        List<LogRecord> records = parser.parse(logLines);

        assertEquals(1, records.size());
        LogRecord record = records.getFirst();
        assertEquals("192.168.1.1", record.remoteAddr());
    }

    @Test
    void testParse_withEmptyStream() {

        Stream<String> logLines = Stream.empty();

        List<LogRecord> records = parser.parse(logLines);

        assertTrue(records.isEmpty());
    }
}
