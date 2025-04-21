package backend.academy.loganalyzer.parser;

import backend.academy.loganalyzer.template.LogRecord;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NginxLogParser {
    private static final String IP_REGEX = "(?<ip>\\S+)";
    private static final String USER_REGEX = "(?<user>\\S+)";
    private static final String TIME_REGEX = "\\[(?<time>[^]]+)]";
    private static final String METHOD_REGEX = "\"(?<method>\\S+)";
    private static final String REQUEST_REGEX = "(?<request>\\S+)";
    private static final String STATUS_REGEX = "(?<status>\\d{3})";
    private static final String BYTES_REGEX = "(?<bytes>\\d+)";
    private static final String AGENT_REGEX = "\"(?<agent>[^\"]*)\"";
    private static final String NOT_QUOTED_REGEX = "[^\"]*\"";
    private static final String QUOTED_REGEX = "\"[^\"]*\"";
    private static final Pattern LOG_PATTERN = Pattern.compile(
        String.join(" ",
            IP_REGEX,
            "-",
            USER_REGEX,
            TIME_REGEX,
            METHOD_REGEX,
            REQUEST_REGEX,
            NOT_QUOTED_REGEX,
            STATUS_REGEX,
            BYTES_REGEX,
            QUOTED_REGEX,
            AGENT_REGEX
        )
    );

    public List<LogRecord> parse(Stream<String> logLines) {
        return logLines
            .map(this::parseLine)
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<LogRecord> parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.matches()) {
            LogRecord logRecord = new LogRecord();
            logRecord.remoteAddr(matcher.group("ip"));
            logRecord.request(matcher.group("request"));
            logRecord.status(Integer.parseInt(matcher.group("status")));
            logRecord.bodyBytesSent(Integer.parseInt(matcher.group("bytes")));
            logRecord.userAgent(matcher.group("agent"));
            logRecord.method(matcher.group("method"));
            String timeStr = matcher.group("time");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            ZonedDateTime zdt = ZonedDateTime.parse(timeStr, fmt);
            logRecord.timestamp(zdt.toLocalDateTime());
            return Optional.of(logRecord);
        }
        return Optional.empty();
    }

}


