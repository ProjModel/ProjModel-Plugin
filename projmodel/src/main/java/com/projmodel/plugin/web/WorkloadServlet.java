package com.projmodel.plugin.web;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorkloadServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;

    @Inject
    public WorkloadServlet(@ComponentImport TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        Map<String, Object> context = new HashMap<>();
        context.put("pluginName", "ProjModel");
        context.put("pageTitle", "Workload — загрузка участников");
        context.put("pageDescription", "Отслеживание загрузки участников команды");
        context.put("status", "MVP-заглушка");
        context.put("statusMessage", "Функционал находится в разработке. Здесь будет отображаться загрузка каждого участника проекта.");

        templateRenderer.render("/templates/workload.vm", context, resp.getWriter());
    }
}