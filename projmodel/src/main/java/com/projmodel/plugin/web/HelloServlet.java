package com.projmodel.plugin.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write("<h1>FIRST Hello from Jira plugin 🚀 бля буду, я это развернула!</h1>");
    }
}
