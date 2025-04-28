package backend.academy.loganalyzer.parser;

import backend.academy.loganalyzer.model.HttpMethod;
import backend.academy.loganalyzer.model.LogRecord;
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
            String remoteAddr = matcher.group("ip");
            String remoteUser = matcher.group("user");
            String timeStr = matcher.group("time");
            String methodStr = matcher.group("method");
            String request = matcher.group("request");
            int status = Integer.parseInt(matcher.group("status"));
            long bodyBytesSent = Long.parseLong(matcher.group("bytes"));
            String userAgent = matcher.group("agent");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr, formatter);

            LogRecord logRecord = new LogRecord(
                remoteAddr,
                "-".equals(remoteUser) ? null : remoteUser,
                zonedDateTime.toLocalDateTime(),
                HttpMethod.fromString(methodStr),
                request,
                status,
                bodyBytesSent,
                null,
                userAgent
            );

            return Optional.of(logRecord);
        }
        return Optional.empty();
    }

}
