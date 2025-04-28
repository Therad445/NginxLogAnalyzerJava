package backend.academy.loganalyzer.model;

import java.time.LocalDateTime;

public record LogRecord(String remoteAddr, String remoteUser, LocalDateTime timeLocal, String method, String request,
                        int status, long bodyBytesSent, String httpReferer, String userAgent, LocalDateTime timestamp) {

    @Override
    public String toString() {
        return "LogRecord[" +
            "remoteAddr=" + remoteAddr + ", " +
            "remoteUser=" + remoteUser + ", " +
            "timeLocal=" + timeLocal + ", " +
            "method=" + method + ", " +
            "request=" + request + ", " +
            "status=" + status + ", " +
            "bodyBytesSent=" + bodyBytesSent + ", " +
            "httpReferer=" + httpReferer + ", " +
            "userAgent=" + userAgent + ", " +
            "timestamp=" + timestamp + ']';
    }
}
