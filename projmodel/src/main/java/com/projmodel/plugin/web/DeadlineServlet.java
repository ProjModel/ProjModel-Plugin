package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.projmodel.plugin.dto.IssueViewDTO;


import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class DeadlineServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;

    @Inject
    public DeadlineServlet(@ComponentImport TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        Map<String, Object> context = new HashMap<>();
        context.put("pluginName", "ProjModel");
        context.put("pageTitle", "Deadline Analysis — анализ дедлайнов");
        context.put("pageDescription", "Визуализация дедлайнов проекта и уровней риска");
        context.put("status", "В разработке");
        context.put("statusMessage", "Функционал находится в разработке.");
        context.put("req", req);

        templateRenderer.render("/templates/deadline.vm", context, resp.getWriter());
    }

    private Map<String, Integer> calculateStats(List<IssueViewDTO> issues) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", issues.size());

        int critical = 0;  // просроченные или дедлайн сегодня
        int high = 0;      // дедлайн в ближайшие 3 дня
        int medium = 0;    // дедлайн в ближайшие 7 дней
        int low = 0;       // остальные

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 3);
        Date threeDaysLater = cal.getTime();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date sevenDaysLater = cal.getTime();

        for (IssueViewDTO issue : issues) {
            Date dueDate = issue.getDueDate();
            if (dueDate == null) {
                low++;
            } else if (dueDate.before(now)) {
                critical++;
            } else if (dueDate.before(threeDaysLater)) {
                high++;
            } else if (dueDate.before(sevenDaysLater)) {
                medium++;
            } else {
                low++;
            }
        }

        stats.put("critical", critical);
        stats.put("high", high);
        stats.put("medium", medium);
        stats.put("low", low);

        return stats;
    }
}