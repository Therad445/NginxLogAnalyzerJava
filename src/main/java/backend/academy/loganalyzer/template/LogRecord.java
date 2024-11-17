package backend.academy.loganalyzer.template;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * LogRecord.java — POJO, представляющий одну запись лога с полями, соответствующими структуре NGINX логов.
 */
@Setter @Getter public class LogRecord {
    private String remoteAddr;
    private String remoteUser;
    private LocalDateTime timeLocal;
    private String method;
    private String request;
    private int status;
    private long bodyBytesSent;
    private String httpReferer;
    private String userAgent;

    public LogRecord() {
    }
}
