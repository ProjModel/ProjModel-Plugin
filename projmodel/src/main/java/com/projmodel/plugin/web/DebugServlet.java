package com.projmodel.plugin.web;

import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.ProjectDataService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;

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
}
