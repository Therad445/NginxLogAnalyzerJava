package backend.academy.loganalyzer.visual;

import backend.academy.loganalyzer.model.LogResult;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;

public class PdfReportGenerator {

    public void generate(LogResult result, String outputPath) throws Exception {
        Document doc = new Document();
        OutputStream out = new FileOutputStream(outputPath);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Отчёт по логам Nginx", titleFont));
        doc.add(new Paragraph("Дата: " + LocalDateTime.now(), normalFont));
        doc.add(new Paragraph("\n"));

        doc.add(new Paragraph("Общая информация:", titleFont));
        Table table = new Table(2);
        table.addCell("Метрика");
        table.addCell("Значение");
        table.addCell("Количество запросов");
        table.addCell(String.valueOf(result.totalRequests()));
        table.addCell("Средний размер ответа");
        table.addCell(String.format("%.2f", result.averageResponseSize()));
        table.addCell("95% перцентиль размера");
        table.addCell(String.format("%.2f", result.percentile()));
        doc.add(table);

        doc.add(new Paragraph("\nАномалии:", titleFont));
        if (result.anomalies().isEmpty()) {
            doc.add(new Paragraph("Не обнаружены", normalFont));
        } else {
            for (var entry : result.anomalies().entrySet()) {
                doc.add(new Paragraph(entry.getKey() + ": " + entry.getValue().size() + " шт.", normalFont));
            }
        }

        doc.add(new Paragraph("\nПодозрительные IP:", titleFont));
        if (result.suspiciousIps().isEmpty()) {
            doc.add(new Paragraph("Не обнаружены", normalFont));
        } else {
            for (String ip : result.suspiciousIps()) {
                doc.add(new Paragraph("• " + ip, normalFont));
            }
        }

        String graphPath = "src/main/resources/static/traffic_errors.png";
        var img = Image.getInstance(graphPath);
        img.scaleToFit(500, 300);
        doc.add(new Paragraph("\n"));
        doc.add(img);

        doc.close();
    }
}
