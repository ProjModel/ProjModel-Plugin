package com.projmodel.plugin.service.impl;

import com.atlassian.jira.issue.Issue;
import com.projmodel.plugin.service.IssueDataService;

import javax.inject.Named;
import java.util.List;

@Named
public class IssueDataServiceImpl implements IssueDataService {
    
    @Override
    public List<Issue> getIssuesForProject(String projectKey) {
        return List.of();
    }

    @Override
    public List<Issue> getOpenIssuesForProject(String projectKey) {
        return List.of();
    }
}
