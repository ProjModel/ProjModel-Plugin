package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.ProjectViewDTO;
import com.projmodel.plugin.service.DeadlineAnalysisService;
import com.projmodel.plugin.service.ProjectDataService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeadlineServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;
    private final ProjectDataService projectDataService;
    private final DeadlineAnalysisService deadlineAnalysisService;

    @Inject
    public DeadlineServlet(@ComponentImport TemplateRenderer templateRenderer,
                           ProjectDataService projectDataService,
                           DeadlineAnalysisService deadlineAnalysisService) {
        this.templateRenderer = templateRenderer;
        this.projectDataService = projectDataService;
        this.deadlineAnalysisService = deadlineAnalysisService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        List<ProjectViewDTO> projects = projectDataService.getAllProjects();

        String projectKey = req.getParameter("projectKey");

        if ((projectKey == null || projectKey.trim().isEmpty()) && !projects.isEmpty()) {
            projectKey = projects.get(0).getKey();
        }

        List<DeadlineIssueDTO> deadlineIssues = null;
        if (projectKey != null && !projectKey.trim().isEmpty()) {
            deadlineIssues = deadlineAnalysisService.analyzeDeadlines(projectKey);
        }

        Map<String, Integer> stats = calculateStats(deadlineIssues);

        Map<String, Object> context = new HashMap<>();
        context.put("projects", projects);
        context.put("projectKey", projectKey);
        context.put("deadlineIssues", deadlineIssues);
        context.put("stats", stats);
        context.put("req", req);

        templateRenderer.render("/templates/deadline.vm", context, resp.getWriter());
    }

    private Map<String, Integer> calculateStats(List<DeadlineIssueDTO> issues) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        if (issues == null) {
            stats.put("total", 0);
            stats.put("critical", 0);
            stats.put("high", 0);
            stats.put("medium", 0);
            stats.put("low", 0);
            stats.put("noDeadline", 0);
            return stats;
        }

        stats.put("total", issues.size());

        int critical = 0, high = 0, medium = 0, low = 0, noDeadline = 0;

        for (DeadlineIssueDTO issue : issues) {
            switch (issue.getRiskLevel()) {
                case "CRITICAL": critical++; break;
                case "HIGH": high++; break;
                case "MEDIUM": medium++; break;
                case "LOW": low++; break;
                case "NO_DEADLINE": noDeadline++; break;
            }
        }

        stats.put("critical", critical);
        stats.put("high", high);
        stats.put("medium", medium);
        stats.put("low", low);
        stats.put("noDeadline", noDeadline);

        return stats;
    }
}