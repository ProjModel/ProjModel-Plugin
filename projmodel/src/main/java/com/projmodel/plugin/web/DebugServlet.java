package com.projmodel.plugin.web;

import com.projmodel.plugin.dto.ProjectViewDTO;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.ProjectDataService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Named
public class DebugServlet extends HttpServlet {

    private final ProjectDataService projectDataService;
    private final IssueDataService issueDataService;

    @Inject
    public DebugServlet(ProjectDataService projectDataService,
                        IssueDataService issueDataService) {
        this.projectDataService = projectDataService;
        this.issueDataService = issueDataService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");

        String projectKey = req.getParameter("projectKey");
        List<ProjectViewDTO> projects = projectDataService.getAllProjects();

        resp.getWriter().println("<h1>Debug Jira Data</h1>");
        resp.getWriter().println("<p>Projects count: " + projects.size() + "</p>");

        if (projectKey != null && !projectKey.trim().isEmpty()) {
            ProjectViewDTO project = projectDataService.getProjectByKey(projectKey);

            if (project == null) {
                resp.getWriter().println("<p>Project not found: " + projectKey + "</p>");
                return;
            }

            resp.getWriter().println("<p>Project key: " + project.getKey() + "</p>");
            resp.getWriter().println("<p>Project name: " + project.getName() + "</p>");
            resp.getWriter().println("<p>Issues count: " +
                    issueDataService.getIssuesForProject(projectKey).size() + "</p>");
            resp.getWriter().println("<p>Open issues count: " +
                    issueDataService.getOpenIssuesForProject(projectKey).size() + "</p>");
        } else {
            resp.getWriter().println("<p>Передай projectKey в URL, например: ?projectKey=TEST</p>");
        }
    }
}
