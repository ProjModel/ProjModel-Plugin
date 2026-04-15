package com.projmodel.plugin.service;

import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * Интерфейс для реализации чтения данных из задач проектов Jira (само чтение)
 */
public interface IssueDataService {

    List<Issue> getIssuesForProject(String projectKey);
    List<Issue> getOpenIssuesForProject(String projectKey);
}
