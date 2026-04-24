package com.projmodel.plugin.web;

import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.service.ReportService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Сервлет для скачивания сгенерированных отчётов
 * Принимает ID отчёта и отдаёт файл нужного формата
 */
public class ReportServlet extends HttpServlet{
}
