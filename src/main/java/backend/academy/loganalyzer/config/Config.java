package backend.academy.loganalyzer.config;

import com.beust.jcommander.Parameter;
import lombok.Getter;

/**
 * Config - класс для конфигурации программы, содержащий аргументы командной строки с JCommander.
 */
@Getter public class Config {
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

    @Parameter(names = "--filter-value", description = "Переменная фильтрации \"Mozila\" или \"GET\"")
    private String filterValue;

    // Надо потом отрефакторить и сделать поля неизменяемыми?
}

