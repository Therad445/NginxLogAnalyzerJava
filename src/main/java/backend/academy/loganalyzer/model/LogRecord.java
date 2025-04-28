package backend.academy.loganalyzer.model;

import backend.academy.loganalyzer.model.HttpMethod;

import java.time.LocalDateTime;

public record LogRecord(
        String remoteAddr,
        String remoteUser,
        LocalDateTime timestamp,
        HttpMethod method,
        String request,
        int status,
        long bodyBytesSent,
        String httpReferer,
        String userAgent
) {
    @Override
    public String toString() {
        return "LogRecord{" +
                "remoteAddr='" + remoteAddr + '\'' +
                ", remoteUser='" + remoteUser + '\'' +
                ", timestamp=" + timestamp +
                ", method=" + method +
                ", request='" + request + '\'' +
                ", status=" + status +
                ", bodyBytesSent=" + bodyBytesSent +
                ", httpReferer='" + httpReferer + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
