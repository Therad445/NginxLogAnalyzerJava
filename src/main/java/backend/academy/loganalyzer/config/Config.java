package backend.academy.loganalyzer.config;

import com.beust.jcommander.Parameter;
import java.time.Duration;
import lombok.Getter;

/**
 * Config - класс для конфигурации программы, содержащий аргументы командной строки с JCommander.
 */
@Getter public class Config {
    private static final int DEFAULT_WINDOW_SECONDS = 20;
    private static final double DEFAULT_Z_THRESHOLD = 3.0;
    @Parameter(names = "--path", description = "Путь к распаложению файлов", required = true)
    private String path;
    @Parameter(names = "--from", description = "Начальная дата в формате ISO8601")
    private String from;
    @Parameter(names = "--to", description = "Конеченая дата в формате ISO8601")
    private String to;
    @Parameter(names = "--format", description = "Формат markdown или adoc")
    private String format;
    @Parameter(names = "--filter-field", description = "Метод фильтрации agent или method")
    private String filterField;
    @Parameter(names = "--filter-value", description = "Переменная фильтрации \"Mozilla\" или \"GET\"")
    private String filterValue;

    public static Duration getAggregationWindow() {
        String v = System.getProperty("windowSeconds");
        return Duration.ofSeconds(v != null ? Integer.parseInt(v) : DEFAULT_WINDOW_SECONDS);
    }

    public static double getZThreshold() {
        String v = System.getProperty("zThreshold");
        return v != null ? Double.parseDouble(v) : DEFAULT_Z_THRESHOLD;
    }
}

