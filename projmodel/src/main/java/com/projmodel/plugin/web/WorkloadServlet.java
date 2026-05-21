package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.projmodel.plugin.dto.ProjectViewDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;
import com.projmodel.plugin.service.ProjectDataService;
import com.projmodel.plugin.service.WorkloadAnalysisService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class WorkloadServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;
    private final WorkloadAnalysisService workloadAnalysisService;
    private final ProjectDataService projectDataService;

    @Inject
    public WorkloadServlet(@ComponentImport TemplateRenderer templateRenderer,
                           WorkloadAnalysisService workloadAnalysisService,
                           ProjectDataService projectDataService) {
        this.templateRenderer = templateRenderer;
        this.workloadAnalysisService = workloadAnalysisService;
        this.projectDataService = projectDataService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        // Получаем все проекты для выпадающего списка
        List<ProjectViewDTO> projects = projectDataService.getAllProjects();

        // Получаем projectKey из параметров URL
        String projectKey = req.getParameter("projectKey");

        // Если projectKey не передан, берем первый проект из списка
        if ((projectKey == null || projectKey.trim().isEmpty()) && !projects.isEmpty()) {
            projectKey = projects.get(0).getKey();
        }

        // Получаем данные анализа загрузки
        List<WorkloadViewDTO> workloadData = null;
        if (projectKey != null && !projectKey.trim().isEmpty()) {
            workloadData = workloadAnalysisService.analyzeWorkload(projectKey);
        }

        // Подготавливаем контекст для шаблона
        Map<String, Object> context = new HashMap<>();
        context.put("projects", projects);
        context.put("projectKey", projectKey);
        context.put("workloadData", workloadData);
        context.put("req", req);

        // Рендерим шаблон
        templateRenderer.render("/templates/workload.vm", context, resp.getWriter());
    }
}