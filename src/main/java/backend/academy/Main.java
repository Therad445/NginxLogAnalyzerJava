package backend.academy;

import backend.academy.loganalyzer.analyzer.LogAnalyzer;
import backend.academy.loganalyzer.config.Config;
import backend.academy.loganalyzer.parser.NginxLogParser;
import backend.academy.loganalyzer.reader.LogPathSelector;
import backend.academy.loganalyzer.report.AsciidocFormat;
import backend.academy.loganalyzer.report.LogReportFormat;
import backend.academy.loganalyzer.report.MarkdownFormat;
import backend.academy.loganalyzer.template.LogResult;
import backend.academy.loganalyzer.template.LogRecord;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import com.beust.jcommander.JCommander;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * Main.java — запускает приложение,
 * обрабатывает аргументы командной строки и передает их
 * в нужные компоненты.
 */
@Log4j2
@UtilityClass
public class Main {
    public static void main(String[] args) {

        // Вввод в JCommander
        Config config = new Config();
        JCommander.newBuilder().addObject(config).build().parse(args);

        //Выбор формата
        LogReportFormat
            formatter = "adoc".equalsIgnoreCase(config.format()) ? new AsciidocFormat() : new MarkdownFormat();

        // Парсинг и анализ логов
        LogResult result = getLogResult(config.path(), config.filterField(), config.filterValue());

        // Формирование и вывод отчета
        log.info(formatter.format(result));
    }

    private static LogResult getLogResult(String path, String filterField, String filterValue) {
        NginxLogParser parser = new NginxLogParser();
        LogAnalyzer analyzer = new LogAnalyzer();
        try {
            // Создаем поток
            Stream<String> stringStream = LogPathSelector.TypeSelector(path);

            // Обрабатываем поток
            List<LogRecord> logs = parser.parse(stringStream);

            // Проверяем условие
            if (filterField != null && filterValue != null) {
                logs = analyzer.filterLogs(logs, filterField, filterValue);
            }

            // Содержимое логов
            long totalRequests = analyzer.countTotalRequests(logs);
            double averageSize = analyzer.averageResponseSize(logs);
            Map<String, Long> resourceCounts = analyzer.countResources(logs);
            Map<Integer, Long> statusCodeCounts = analyzer.countStatusCodes(logs);

            // Создание результата анализа
            return new LogResult(totalRequests, averageSize, resourceCounts, statusCodeCounts);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }
}
