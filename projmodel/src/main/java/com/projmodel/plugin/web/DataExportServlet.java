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

public class DataExportServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;

    @Inject
    public DataExportServlet(@ComponentImport TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        Map<String, Object> context = new HashMap<>();
        context.put("pluginName", "ProjModel");
        context.put("pageTitle", "Data Export - экспорт данных");
        context.put("pageDescription", "Временная страница-заглушка для будущего экспорта данных проекта и отчетов");
        context.put("status", "MVP / в разработке");
        context.put("statusMessage", "Здесь будет страница генерации и скачивания экспортов. Пока это рабочая заглушка для навигации и демонстрации будущего сценария.");
        context.put("req", req);

        templateRenderer.render("/templates/export.vm", context, resp.getWriter());
    }
}
