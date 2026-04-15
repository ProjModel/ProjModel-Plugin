package com.projmodel.plugin.service.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.service.IssueDataService;

import javax.inject.Named;
import com.atlassian.query.Query;
import java.util.Collections;
import java.util.List;

/**
 * Сервис для получения данных о задачах проектов в Jira с использованием языка запросов Jira - JQL
 */
@Named
public class IssueDataServiceImpl implements IssueDataService {

    /**
     * Сервис Jira для выполнения JQL-запросов
     */
    private final SearchService _searchService;

    /**
     * Конструктор сервиса
     * @param service сервис для выполнения JQL-запросов
     */
    public IssueDataServiceImpl(SearchService service) {
        _searchService = service;
    }

    /**
     * Получить список задач проекта по уникальному ключу проекта (например, проект "TEST")
     * @param projectKey уникальный ключ проекта
     * @return список задач проекта
     */
    @Override
    public List<Issue> getIssuesForProject(String projectKey) {
        String jql = "project = \"" + projectKey + "\" ORDER BY created DESC";
        return searchIssuesByJql(jql);
    }

    /**
     * Получить список незавершенных задач проекта по уникальному ключу проекта
     * @param projectKey уникальный ключ проекта
     * @return список незавершенных задач проекта
     */
    @Override
    public List<Issue> getOpenIssuesForProject(String projectKey) {
        String jql = "project = \"" + projectKey + "\" AND resolution = Unresolved ORDER BY due ASC";
        return searchIssuesByJql(jql);
    }

    /**
     * Получить список незавершенных задач по jql строке
     * @param jql строка jql
     * @return список незавершенных задач
     */
    private List<Issue> searchIssuesByJql(String jql) {
        try {
            //получаем юзера
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            if(user == null) {
                return Collections.emptyList();
            }

            //получаем и парсим строку jql
            SearchService.ParseResult parseResult = _searchService.parseQuery(user, jql);
            if(!parseResult.isValid()) {
                return Collections.emptyList();
            }

            Query q = parseResult.getQuery();
            //сортировка по только незавершенным задачам
            SearchResults<Issue> results = _searchService.search(user, q, com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter());

            return results.getResults();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
