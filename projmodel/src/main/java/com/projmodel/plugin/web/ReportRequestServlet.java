package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервлет для обработки запросов на создание и скачивание отчётов
 * Обрабатывает форму с главной страницы: создаёт отчёт и сразу отдаёт файл
 */
public class ReportRequestServlet extends HttpServlet {

    private final ReportService _reportService;
    private final ProjectDataService _projectDataService;
    private final IssueDataService _issueDataService;
    private final TemplateRenderer _templateRenderer;

    @Inject
    public ReportRequestServlet(ReportService reportService,
                                ProjectDataService projectDataService,
                                IssueDataService issueDataService,
                                @ComponentImport TemplateRenderer templateRenderer) {
        _reportService = reportService;
        _projectDataService = projectDataService;
        _issueDataService = issueDataService;
        _templateRenderer = templateRenderer;
    }

    /**
     * Обработка GET-запроса: отображение формы выбора проекта
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String projectKey = req.getParameter("projectKey");

        List<IssueViewDTO> issues = null;
        if (projectKey != null && !projectKey.isBlank()) {
            //если выбран проект - показываем список задач
            issues = _issueDataService.getIssuesForProject(projectKey);
        }

        //получаем список всех проектов для выпадающего списка
        List<ProjectViewDTO> projects = _projectDataService.getAllProjects();

        // Подготавливаем контекст для шаблона
        Map<String, Object> context = new HashMap<>();
        context.put("projectKey", projectKey);
        context.put("issues", issues);
        context.put("projects", projects);
        context.put("req", req);

        resp.setContentType("text/html;charset=UTF-8");
        _templateRenderer.render("/templates/report-form.vm", context, resp.getWriter());
    }

    /**
     * Обработка POST-запроса: создание отчёта и отправка файла
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            //получаем параметры из формы
            String projectKey = req.getParameter("projectKey");
            String format = req.getParameter("format");
            String[] selectedIssues = req.getParameterValues("issues");

            // ОТЛАДКА: выводим в консоль, что пришло
            System.out.println("=== REPORT REQUEST DEBUG ===");
            System.out.println("projectKey: " + projectKey);
            System.out.println("format: " + format);
            System.out.println("selectedIssues: " + (selectedIssues != null ? selectedIssues.length : "null"));

            //проверяем, что все данные заполнены
            if (projectKey == null || format == null || selectedIssues == null || selectedIssues.length == 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Заполните все поля формы. projectKey=" + projectKey +
                                ", format=" + format +
                                ", issues=" + (selectedIssues == null ? "null" : selectedIssues.length));
                return;
            }

            //создаём запрос на отчёт в БД
            List<String> issueKeys = Arrays.asList(selectedIssues);
            ReportTaskAO report = _reportService.createReportRequest(projectKey, issueKeys, format);

            //сразу генерируем файл
            byte[] fileData = _reportService.generateReportFile(report.getID());

            //формируем имя файла
            if ("WORD".equals(format)) {
                String fileName = "report_" + projectKey + "_" +
                        report.getCreatedDate().getTime() + ".html";
                resp.setContentType("text/html; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            } else {
                String fileName = "report_" + projectKey + "_" +
                        report.getCreatedDate().getTime() + ".txt";
                resp.setContentType("text/plain; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }

            resp.setCharacterEncoding("UTF-8");
            resp.setContentLength(fileData.length);

            //отправляем файл
            try (OutputStream out = resp.getOutputStream()) {
                out.write(fileData);
                out.flush();
            }

            System.out.println("=== REPORT GENERATED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("=== REPORT GENERATION ERROR ===");
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка при создании отчёта: " + e.getMessage());
        }
    }
}