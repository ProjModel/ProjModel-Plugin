package com.projmodel.plugin.report;

import com.projmodel.plugin.dto.IssueViewDTO;
import org.apache.poi.xwpf.usermodel.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

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

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            //создаем PDF-документ
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            //добавляем заголовок
            Paragraph title = new Paragraph("Отчёт по проекту " + projectKey)
                    .setBold()
                    .setFontSize(16);
            document.add(title);

            //добавляем дату создания
            Paragraph date = new Paragraph("Создан: " +
                    new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date()))
                    .setFontSize(10);
            document.add(date);

            //пустая строка для отступа
            document.add(new Paragraph(""));

            //создаем таблицу (5 колонок)
            Table table = new Table(5);

            //заголовки таблицы (жирным шрифтом)
            String[] headers = {"Ключ", "Название", "Статус", "Исполнитель", "Дедлайн"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold());
                table.addCell(cell);
            }

            //заполняем таблицу данными задач
            for (IssueViewDTO issue : issues) {
                table.addCell(new Cell().add(new Paragraph(issue.get_key())));
                table.addCell(new Cell().add(new Paragraph(issue.get_summary())));
                table.addCell(new Cell().add(new Paragraph(issue.get_status())));
                table.addCell(new Cell().add(new Paragraph(issue.getAssignee())));
                table.addCell(new Cell().add(new Paragraph(
                        issue.getDueDate() != null ?
                                new SimpleDateFormat("dd.MM.yyyy").format(issue.getDueDate()) : "Нет")));
            }

            //добавляем таблицу в документ
            document.add(table);
            document.close();

            return baos.toByteArray();
        }
    }
}
