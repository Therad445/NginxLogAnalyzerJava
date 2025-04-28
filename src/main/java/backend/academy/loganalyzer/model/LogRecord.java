package backend.academy.loganalyzer.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogRecord {
    private String remoteAddr;
    private String remoteUser;
    private LocalDateTime timeLocal;
    private String method;
    private String request;
    private int status;
    private long bodyBytesSent;
    private String httpReferer;
    private String userAgent;
    private LocalDateTime timestamp;
}
