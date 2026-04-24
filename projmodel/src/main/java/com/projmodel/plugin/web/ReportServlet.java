package com.projmodel.plugin.web;

import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.service.ReportService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Сервлет для скачивания сгенерированных отчётов
 * Принимает ID отчёта и отдаёт файл нужного формата
 */
public class ReportServlet extends HttpServlet{
    /**
     * Сервис для работы с отчётами
     */
    private final ReportService _reportService;

    /**
     * Конструктор сервлета
     * @param reportService сервис для генерации и получения отчётов
     */
    @Inject
    public ReportServlet(ReportService reportService) {
        _reportService = reportService;
    }

    /**
     * Обработка GET-запроса на скачивание отчёта
     * Ожидает параметр id - идентификатор запроса на отчёт
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //получаем ID отчёта из параметров запроса
        String reportIdStr = req.getParameter("id");

        //если ID не передан, возвращаем ошибку
        if (reportIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не указан ID отчёта");
            return;
        }

        //преобразуем строку в число
        int reportId = Integer.parseInt(reportIdStr);

        //получаем запрос на отчёт из БД
        ReportTaskAO report = _reportService.getReportById(reportId);

        //если отчёт не найден, возвращаем 404
        if (report == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Отчёт не найден");
            return;
        }

        //генерируем файл отчёта
        byte[] fileData = _reportService.generateReportFile(reportId);

        //формируем имя файла для скачивания
        String fileExtension = "WORD".equals(report.getReportFormat()) ? ".docx" : ".pdf";
        String fileName = "report_" + report.getProjectKey() + "_" +
                report.getCreatedDate().getTime() + fileExtension;

        //настраиваем HTTP-ответ для скачивания файла
        if ("WORD".equals(report.getReportFormat())) {
            resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else {
            resp.setContentType("application/pdf");
        }

        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLength(fileData.length);

        //отправляем файл в ответ
        try (OutputStream out = resp.getOutputStream()) {
            out.write(fileData);
            out.flush();
        }
    }
}
