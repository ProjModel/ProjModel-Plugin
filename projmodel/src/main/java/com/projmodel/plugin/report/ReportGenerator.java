package com.projmodel.plugin.report;

import com.projmodel.plugin.dto.IssueViewDTO;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Утилитный класс для генерации отчётов в форматах Word и PDF
 * Содержит статические методы для создания файлов отчётов
 */
public class ReportGenerator {
    /**
     * Сгенерировать Word-отчёт (.docx) по списку задач
     * Создаёт документ с заголовком, датой и таблицей задач
     * @param issues список задач для включения в отчёт
     * @param projectKey ключ проекта
     * @return массив байтов готового Word-документа
     * @throws IOException при ошибке создания файла
     */
    public static byte[] generateWordReport(List<IssueViewDTO> issues, String projectKey)
            throws IOException {

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            //создаем заголовок отчёта
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Отчёт по проекту " + projectKey);
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            //добавляем дату создания отчёта
            XWPFParagraph date = document.createParagraph();
            XWPFRun dateRun = date.createRun();
            dateRun.setText("Создан: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date()));
            dateRun.setFontSize(10);

            //пустая строка для отступа перед таблицей
            document.createParagraph();

            //создаем таблицу с данными задач
            XWPFTable table = document.createTable();

            //формируем заголовки таблицы
            String[] headers = {"Ключ", "Название", "Статус", "Исполнитель", "Дедлайн"};
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                setCellText(cell, headers[i], true);
            }

            //заполняем таблицу данными задач
            for (IssueViewDTO issue : issues) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(issue.get_key());
                row.getCell(1).setText(issue.get_summary());
                row.getCell(2).setText(issue.get_status());
                row.getCell(3).setText(issue.getAssignee());
                row.getCell(4).setText(issue.getDueDate() != null ?
                        new SimpleDateFormat("dd.MM.yyyy").format(issue.getDueDate()) : "Нет");
            }

            //сохраняем документ в массив байтов
            document.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Вспомогательный метод для установки текста в ячейке Word-таблицы
     * @param cell ячейка таблицы
     * @param text текст для отображения
     * @param bold true - жирный шрифт, false - обычный
     */
    private static void setCellText(XWPFTableCell cell, String text, boolean bold) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(10);
    }

    /**
     * Сгенерировать PDF-отчёт по списку задач
     * Создаёт PDF-документ с заголовком, датой и таблицей задач
     * @param issues список задач для включения в отчёт
     * @param projectKey ключ проекта
     * @return массив байтов готового PDF-документа
     * @throws IOException при ошибке создания файла
     */
    public static byte[] generatePdfReport(List<IssueViewDTO> issues, String projectKey)
            throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            //создаем страницу
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            //поток для записи содержимого
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            //шрифт (встроенный)
            PDType0Font font = PDType0Font.load(document,
                    ReportGenerator.class.getResourceAsStream("/fonts/DejaVuSans.ttf"));
            PDType0Font fontBold = PDType0Font.load(document,
                    ReportGenerator.class.getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"));

            float yPosition = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            float fontSize = 10;
            float titleFontSize = 18;

            //заголовок
            contentStream.beginText();
            contentStream.setFont(fontBold, titleFontSize);
            contentStream.newLineAtOffset(margin + 100, yPosition);
            contentStream.showText("Отчёт по проекту " + projectKey);
            contentStream.endText();

            //дата
            yPosition -= 30;
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Создан: " +
                    new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date()));
            contentStream.endText();

            //таблица
            yPosition -= 30;
            float[] colWidths = {80, 200, 80, 100, 80}; //ширины колонок
            String[] headers = {"Ключ", "Название", "Статус", "Исполнитель", "Дедлайн"};

            //заголовки таблицы
            float xPosition = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.setFont(fontBold, fontSize);
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += colWidths[i];
            }

            //данные
            yPosition -= 20;
            for (IssueViewDTO issue : issues) {
                //проверяем, нужна ли новая страница
                if (yPosition < 50) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - 50;
                }

                xPosition = margin;
                String[] rowData = {
                        issue.get_key(),
                        issue.get_summary(),
                        issue.get_status(),
                        issue.getAssignee(),
                        issue.getDueDate() != null ?
                                new SimpleDateFormat("dd.MM.yyyy").format(issue.getDueDate()) : "Нет"
                };

                for (int i = 0; i < rowData.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    //обрезаем длинный текст чтобы не вылезал за колонку
                    String text = rowData[i] != null && rowData[i].length() > 30
                            ? rowData[i].substring(0, 27) + "..."
                            : rowData[i];
                    contentStream.showText(text);
                    contentStream.endText();
                    xPosition += colWidths[i];
                }
                yPosition -= 20;
            }

            contentStream.close();

            //сохраняем документ в массив байтов
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
