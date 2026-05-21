package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.projmodel.plugin.ai.AIAnalyticsService;
import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.ProjectViewDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;
import com.projmodel.plugin.service.DeadlineAnalysisService;
import com.projmodel.plugin.service.ProjectDataService;
import com.projmodel.plugin.service.WorkloadAnalysisService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIAnalyticsServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;
    private final ProjectDataService projectDataService;
    private final DeadlineAnalysisService deadlineAnalysisService;
    private final WorkloadAnalysisService workloadAnalysisService;
    private final AIAnalyticsService aiAnalyticsService;

    @Inject
    public AIAnalyticsServlet(@ComponentImport TemplateRenderer templateRenderer,
                              ProjectDataService projectDataService,
                              DeadlineAnalysisService deadlineAnalysisService,
                              WorkloadAnalysisService workloadAnalysisService) {
        this.templateRenderer = templateRenderer;
        this.projectDataService = projectDataService;
        this.deadlineAnalysisService = deadlineAnalysisService;
        this.workloadAnalysisService = workloadAnalysisService;
        this.aiAnalyticsService = new AIAnalyticsService();  // ← Создаем вручную
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

        String analysisType = req.getParameter("type");
        if (analysisType == null) {
            analysisType = "general";
        }

        Map<String, Object> context = new HashMap<>();
        context.put("projects", projects);
        context.put("projectKey", projectKey);
        context.put("analysisType", analysisType);
        context.put("req", req);

        if (projectKey != null && !projectKey.trim().isEmpty()) {
            List<DeadlineIssueDTO> deadlineIssues = deadlineAnalysisService.analyzeDeadlines(projectKey);
            List<WorkloadViewDTO> workloadData = workloadAnalysisService.analyzeWorkload(projectKey);

            context.put("deadlineIssues", deadlineIssues);
            context.put("workloadData", workloadData);

            try {
                String aiAnalysis = "";

                if ("deadlines".equals(analysisType)) {
                    aiAnalysis = aiAnalyticsService.analyzeDeadlines(deadlineIssues);
                } else if ("workload".equals(analysisType)) {
                    aiAnalysis = aiAnalyticsService.analyzeWorkload(workloadData);
                } else {
                    aiAnalysis = aiAnalyticsService.getGeneralRecommendations(deadlineIssues, workloadData);
                }

                context.put("aiAnalysis", aiAnalysis);

            } catch (Exception e) {
                context.put("aiError", "Ошибка при получении AI-анализа: " + e.getMessage());
                e.printStackTrace();
            }
        }

        templateRenderer.render("/templates/ai-analytics.vm", context, resp.getWriter());
    }
}