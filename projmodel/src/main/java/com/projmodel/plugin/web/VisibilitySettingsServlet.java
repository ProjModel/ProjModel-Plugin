package com.projmodel.plugin.web;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.projmodel.plugin.service.VisibilityService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Named
public class VisibilitySettingsServlet extends HttpServlet {

    private final VisibilityService _visibilityService;
    private final ProjectManager _projectManager;

    @Inject
    public VisibilitySettingsServlet(
            VisibilityService visibilityService,
            @ComponentImport ProjectManager projectManager
    ) {
        _visibilityService = visibilityService;
        _projectManager = projectManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");

        String projectKey = req.getParameter("projectKey");

        String enabledParam = req.getParameter("enabled");

        if (enabledParam != null) {
            _visibilityService.setEnabled(Boolean.parseBoolean(enabledParam)); //чтоб страница обновлялось и правильно показывала 
        }

        resp.getWriter().println("<h1>ProjModel Visibility Settings</h1>");
        resp.getWriter().println("<p>Фильтр сейчас: <b>" +
                (_visibilityService.isEnabled() ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН") +
                "</b></p>");

        resp.getWriter().println("<a href='?enabled=true'>Включить фильтр</a><br>");
        resp.getWriter().println("<a href='?enabled=false'>Отключить фильтр</a><br><br>");

        resp.getWriter().println("<form method='get'>");
        resp.getWriter().println("<label>Project key: </label>");
        resp.getWriter().println("<input name='projectKey' value='" + safe(projectKey) + "' placeholder='TEST'>");
        resp.getWriter().println("<button type='submit'>Открыть правила</button>");
        resp.getWriter().println("</form>");

        Project project = null;

        if (projectKey != null && !projectKey.trim().isEmpty()) {
            project = _projectManager.getProjectObjByKey(projectKey.trim().toUpperCase());
        }

        if (project != null) {
            resp.getWriter().println("<h2>Настройки проекта " + safe(project.getName()) + "</h2>");

            resp.getWriter().println(
                    "<p>" +
                            "Автоматическая фильтрация включена. " +
                            "Пользователь видит задачу, если один из labels задачи " +
                            "совпадает с частью username пользователя." +
                            "</p>"
            );

            resp.getWriter().println("<h3>Временный доступ</h3>");
            resp.getWriter().println("<form method='post'>");
            resp.getWriter().println("<input type='hidden' name='action' value='temporaryAccess'>");
            resp.getWriter().println("<input type='hidden' name='projectKey' value='" + safe(projectKey) + "'>");
            resp.getWriter().println("<p>Issue key: <input name='issueKey' placeholder='TEST-1'></p>");
            resp.getWriter().println("<p>Username: <input name='username' placeholder='design_alla'></p>");
            resp.getWriter().println("<p>Hours: <input name='hours' type='number' min='1' max='720' value='24'></p>");
            resp.getWriter().println("<button type='submit'>Выдать временный доступ</button>");
            resp.getWriter().println("</form>");
        }
        else if (projectKey != null && !projectKey.trim().isEmpty()) {

            resp.getWriter().println(
                    "<p style='color:red;'>Проект с ключом <b>"
                            + safe(projectKey)
                            + "</b> не найден.</p>"
            );
        }

        resp.getWriter().println("<br><a href='" + req.getContextPath() + "/plugins/servlet/projmodel'>На главную</a>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        String action = req.getParameter("action");
        String projectKey = req.getParameter("projectKey");

        if ("temporaryAccess".equals(action)) {
            String issueKey = req.getParameter("issueKey");
            String username = req.getParameter("username");
            int hours = Integer.parseInt(req.getParameter("hours"));

            _visibilityService.grantTemporaryAccess(projectKey, issueKey, username, hours, user);
        }

        resp.sendRedirect(req.getContextPath()
                + "/plugins/servlet/projmodel/visibility-settings?projectKey="
                + projectKey);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
