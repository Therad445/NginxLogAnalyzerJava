package io.github.therad445.loganalyzer.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpMethodTest {

    @Test
    void fromString_knownMethods_shouldReturnCorrectEnum() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("get"));
        assertEquals(HttpMethod.POST, HttpMethod.fromString("POST"));
        assertEquals(HttpMethod.PUT, HttpMethod.fromString("Put"));
        assertEquals(HttpMethod.DELETE, HttpMethod.fromString("DeLeTe"));
        assertEquals(HttpMethod.PATCH, HttpMethod.fromString("patch"));
        assertEquals(HttpMethod.HEAD, HttpMethod.fromString("HEAD"));
        assertEquals(HttpMethod.OPTIONS, HttpMethod.fromString("OPTIONS"));
        assertEquals(HttpMethod.TRACE, HttpMethod.fromString("TrAcE"));
        assertEquals(HttpMethod.CONNECT, HttpMethod.fromString("CONNECT"));
    }

    @Test
    void fromString_unknownMethod_shouldReturnUNKNOWN() {
        assertEquals(HttpMethod.UNKNOWN, HttpMethod.fromString("foo"));
        assertEquals(HttpMethod.UNKNOWN, HttpMethod.fromString(""));
        assertEquals(HttpMethod.UNKNOWN, HttpMethod.fromString("123"));
        assertEquals(HttpMethod.UNKNOWN, HttpMethod.fromString("po_st"));
    }

    @Test
    void fromString_nullInput_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> HttpMethod.fromString(null));
    }
}
