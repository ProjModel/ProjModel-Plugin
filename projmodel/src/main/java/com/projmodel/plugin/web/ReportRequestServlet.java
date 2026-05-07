package com.projmodel.plugin.web;

import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.dto.ProjectViewDTO;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.service.ProjectDataService;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.ReportService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Сервлет для обработки запросов на создание и скачивание отчётов
 * Обрабатывает форму с главной страницы: создаёт отчёт и сразу отдаёт файл
 */
public class ReportRequestServlet extends HttpServlet {

    private final ReportService _reportService;
    private final ProjectDataService _projectDataService;
    private final IssueDataService _issueDataService;

    @Inject
    public ReportRequestServlet(ReportService reportService,
                                ProjectDataService projectDataService,
                                IssueDataService issueDataService) {
        _reportService = reportService;
        _projectDataService = projectDataService;
        _issueDataService = issueDataService;
    }

    /**
     * Обработка GET-запроса: отображение формы выбора проекта
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String projectKey = req.getParameter("projectKey");

        if (projectKey != null && !projectKey.isBlank()) {
            //если выбран проект - показываем список задач
            List<IssueViewDTO> issues = _issueDataService.getIssuesForProject(projectKey);
            req.setAttribute("projectKey", projectKey);
            req.setAttribute("issues", issues);
        }

        //получаем список всех проектов для выпадающего списка
        List<ProjectViewDTO> projects = _projectDataService.getAllProjects();
        req.setAttribute("projects", projects);

        req.getRequestDispatcher("/templates/report-form.vm").forward(req, resp);
    }

    /**
     * Обработка POST-запроса: создание отчёта и отправка файла
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //получаем параметры из формы
        String projectKey = req.getParameter("projectKey");
        String format = req.getParameter("format");
        String[] selectedIssues = req.getParameterValues("issues");

        //проверяем, что все данные заполнены
        if (projectKey == null || format == null || selectedIssues == null || selectedIssues.length == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Заполните все поля формы");
            return;
        }

        //создаём запрос на отчёт в БД
        List<String> issueKeys = Arrays.asList(selectedIssues);
        ReportTaskAO report = _reportService.createReportRequest(projectKey, issueKeys, format);

        //сразу генерируем файл
        byte[] fileData = _reportService.generateReportFile(report.getID());

        //формируем имя файла
        String fileExtension = "WORD".equals(format) ? ".docx" : ".pdf";
        String fileName = "report_" + projectKey + "_" +
                report.getCreatedDate().getTime() + fileExtension;

        //настраиваем HTTP-ответ для скачивания
        if ("WORD".equals(format)) {
            resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else {
            resp.setContentType("application/pdf");
        }

        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLength(fileData.length);

        //отправляем файл
        try (OutputStream out = resp.getOutputStream()) {
            out.write(fileData);
            out.flush();
        }
    }
}