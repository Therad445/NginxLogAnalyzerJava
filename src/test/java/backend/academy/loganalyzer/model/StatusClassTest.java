package backend.academy.loganalyzer.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusClassTest {

    @Test
    void fromStatusCode_shouldReturnCorrectStatusClass() {
        assertEquals(StatusClass.INFORMATIONAL, StatusClass.fromStatusCode(100));
        assertEquals(StatusClass.SUCCESS, StatusClass.fromStatusCode(204));
        assertEquals(StatusClass.REDIRECTION, StatusClass.fromStatusCode(302));
        assertEquals(StatusClass.CLIENT_ERROR, StatusClass.fromStatusCode(404));
        assertEquals(StatusClass.SERVER_ERROR, StatusClass.fromStatusCode(503));
    }

    @Test
    void fromStatusCode_shouldReturnUnknownForInvalidCodes() {
        assertEquals(StatusClass.UNKNOWN, StatusClass.fromStatusCode(0));
        assertEquals(StatusClass.UNKNOWN, StatusClass.fromStatusCode(99));
        assertEquals(StatusClass.UNKNOWN, StatusClass.fromStatusCode(600));
        assertEquals(StatusClass.UNKNOWN, StatusClass.fromStatusCode(-100));
    }

    @Test
    void getCodePrefix_shouldReturnCorrectPrefix() {
        assertEquals(1, StatusClass.INFORMATIONAL.codePrefix());
        assertEquals(2, StatusClass.SUCCESS.codePrefix());
        assertEquals(3, StatusClass.REDIRECTION.codePrefix());
        assertEquals(4, StatusClass.CLIENT_ERROR.codePrefix());
        assertEquals(5, StatusClass.SERVER_ERROR.codePrefix());
        assertEquals(0, StatusClass.UNKNOWN.codePrefix());
    }
}
