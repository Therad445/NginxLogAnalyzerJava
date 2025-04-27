package backend.academy.loganalyzer.config;

import com.beust.jcommander.Parameter;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Config — класс для разбора аргументов командной строки при запуске NginxLogAnalyzer.
 */
@Setter
@Getter
public class Config {
    private static final int DEFAULT_WINDOW_SECONDS = 20;
    private static final double DEFAULT_Z_THRESHOLD = 3.0;

    @Parameter(
        names = {"--source", "-src"},
        description = "Источник данных: kafka | file | url",
        required = true
    )
    private String source;

    @Parameter(
        names = {"--stream", "-s"},
        description = "Непрерывный режим (только для file или kafka)"
    )
    private boolean streamingMode = false;

    @Parameter(
        names = {"--bootstrap", "-b"},
        description = "Сервер(а) bootstrap.servers для Kafka"
    )
    private String kafkaBootstrapServers;

    @Parameter(
        names = {"--ktopic"},
        description = "Kafka topic"
    )
    private String kafkaTopic;

    @Parameter(
        names = {"--groupId", "-gid"},
        description = "Kafka consumer.group.id"
    )
    private String kafkaGroupId;

    @Parameter(
        names = {"--path", "-i"},
        description = "Путь к лог-файлу (file) или URL (url)",
        required = true
    )
    private String path;

    @Parameter(
        names = {"--from"},
        description = "Начальная дата (ISO-8601), например 2025-04-01T00:00:00"
    )
    private String from;

    @Parameter(
        names = {"--to"},
        description = "Конечная дата (ISO-8601), например 2025-04-26T23:59:59"
    )
    private String to;

    @Parameter(
        names = {"--format", "-f"},
        description = "Формат вывода: markdown или adoc"
    )
    private String format = "markdown";

    @Parameter(
        names = {"--filter-field"},
        description = "Поле для фильтрации (agent, method, remoteAddr и т.п.)"
    )
    private String filterField;

    @Parameter(
        names = {"--filter-value"},
        description = "Значение для фильтрации, например \"Mozilla\" или \"GET\""
    )
    private String filterValue;

    @Parameter(
        names = {"--filter-ip"},
        description = "Фильтрация по IP-адресу (remoteAddr)"
    )
    private String filterIp;

    @Parameter(
        names = {"--export-csv"},
        description = "Путь для сохранения отчёта в CSV"
    )
    private String exportCsv;

    @Parameter(
        names = {"--export-json"},
        description = "Путь для сохранения отчёта в JSON"
    )
    private String exportJson;

    public static Duration aggregationWindow() {
        String v = System.getProperty("windowSeconds");
        return Duration.ofSeconds(v != null
            ? Integer.parseInt(v)
            : DEFAULT_WINDOW_SECONDS);
    }

    public static double zThreshold() {
        String v = System.getProperty("zThreshold");
        return v != null
            ? Double.parseDouble(v)
            : DEFAULT_Z_THRESHOLD;
    }
}
