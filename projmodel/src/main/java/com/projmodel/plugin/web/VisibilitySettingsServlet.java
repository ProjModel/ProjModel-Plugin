package com.projmodel.plugin.web;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.dto.VisibilityRuleDTO;
import com.projmodel.plugin.service.ProjectDataService;
import com.projmodel.plugin.service.VisibilityService;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VisibilitySettingsServlet extends HttpServlet {

    private final VisibilityService _visibilityService;
    private final ProjectDataService _projectDataService;

    @Inject
    public VisibilitySettingsServlet(VisibilityService visibilityService,ProjectDataService projectDataService) {
        _visibilityService = visibilityService;
        _projectDataService = projectDataService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");

        String projectKey = req.getParameter("projectKey");

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

        if (projectKey != null && !projectKey.trim().isEmpty()) {
            List<VisibilityRuleDTO> rules = _visibilityService.getRulesForProject(projectKey);

            resp.getWriter().println("<h2>Правила проекта " + safe(projectKey) + "</h2>");

            if (rules.isEmpty()) {
                resp.getWriter().println("<p>Правил пока нет.</p>");
            } else {
                resp.getWriter().println("<ul>");
                for (VisibilityRuleDTO rule : rules) {
                    resp.getWriter().println("<li>");
                    resp.getWriter().println("role = <b>" + safe(rule.getRoleName()) + "</b>, labels = " +
                            safe(String.join(", ", rule.getAllowedLabels())) +
                            ", enabled = " + rule.isEnabled());
                    resp.getWriter().println("</li>");
                }
                resp.getWriter().println("</ul>");
            }

            resp.getWriter().println("<h3>Добавить / обновить правило</h3>");
            resp.getWriter().println("<form method='post'>");
            resp.getWriter().println("<input type='hidden' name='action' value='saveRule'>");
            resp.getWriter().println("<input type='hidden' name='projectKey' value='" + safe(projectKey) + "'>");
            resp.getWriter().println("<p>Role: <input name='roleName' placeholder='frontend'></p>");
            resp.getWriter().println("<p>Labels: <input name='labels' placeholder='frontend, ui'></p>");
            resp.getWriter().println("<p>Enabled: <input type='checkbox' name='enabled' checked></p>");
            resp.getWriter().println("<button type='submit'>Сохранить правило</button>");
            resp.getWriter().println("</form>");

            resp.getWriter().println("<h3>Временный доступ</h3>");
            resp.getWriter().println("<form method='post'>");
            resp.getWriter().println("<input type='hidden' name='action' value='temporaryAccess'>");
            resp.getWriter().println("<input type='hidden' name='projectKey' value='" + safe(projectKey) + "'>");
            resp.getWriter().println("<p>Issue key: <input name='issueKey' placeholder='TEST-1'></p>");
            resp.getWriter().println("<p>Username: <input name='username' placeholder='front-user'></p>");
            resp.getWriter().println("<p>Hours: <input name='hours' type='number' min='1' max='720' value='24'></p>");
            resp.getWriter().println("<button type='submit'>Выдать временный доступ</button>");
            resp.getWriter().println("</form>");
        }

        resp.getWriter().println("<br><a href='" + req.getContextPath() + "/plugins/servlet/projmodel'>На главную</a>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        String action = req.getParameter("action");
        String projectKey = req.getParameter("projectKey");

        if ("saveRule".equals(action)) {
            String roleName = req.getParameter("roleName");
            String labelsRaw = req.getParameter("labels");
            boolean enabled = req.getParameter("enabled") != null;

            List<String> labels = Arrays.stream(labelsRaw.split(","))
                    .map(String::trim)
                    .filter(label -> !label.isEmpty())
                    .collect(Collectors.toList());

            VisibilityRuleDTO rule = new VisibilityRuleDTO(projectKey, roleName, labels, enabled);
            _visibilityService.saveRule(rule, user);
        }

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
