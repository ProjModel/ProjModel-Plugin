package com.projmodel.plugin.service.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.service.IssueDataService;

import javax.inject.Inject;
import javax.inject.Named;
import com.atlassian.query.Query;
import com.projmodel.plugin.service.VisibilityService;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
     * Сервис для фильтрации задач по области видимости (по ролям)
     */
    private final VisibilityService _visibilityService;

    /**
     * Конструктор сервиса
     * @param service сервис для выполнения JQL-запросов
     */
    @Inject
    public IssueDataServiceImpl(@ComponentImport SearchService service, VisibilityService visibilityService) {
        _searchService = service;
        _visibilityService = visibilityService;
    }

    /**
     * Получить список задач проекта по уникальному ключу проекта (например, проект "TEST")
     * @param projectKey уникальный ключ проекта
     * @return список задач проекта в формате DTO
     */
    @Override
    public List<IssueViewDTO> getIssuesForProject(String projectKey) {
        String jql = "project = \"" + projectKey + "\" ORDER BY created DESC";
        List<Issue> issues = searchIssuesByJql(projectKey, jql);

        return issues.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить список незавершенных задач проекта по уникальному ключу проекта
     * @param projectKey уникальный ключ проекта
     * @return список незавершенных задач проекта в формате DTO
     */
    @Override
    public List<IssueViewDTO> getOpenIssuesForProject(String projectKey) {
        String jql = "project = \"" + projectKey + "\" AND resolution = Unresolved ORDER BY due ASC";
        List<Issue> issues = searchIssuesByJql(projectKey, jql);

        return issues.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить список незавершенных задач по jql строке
     * @param jql строка jql
     * @return список незавершенных задач
     */
    private List<Issue> searchIssuesByJql(String projectKey, String jql) {
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

            return _visibilityService.filterIssues(projectKey, results.getResults(), user);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Конвертировать Issue в DTO-объект
     * @param issue задача, которую необходимо конвертировать
     * @return сконвертированная задача
     */
    private IssueViewDTO mapToDTO(Issue issue) {
        String key = issue.getKey();
        String summary = issue.getSummary();
        String status = issue.getStatus().getName();

        String assignee = issue.getAssignee() != null
                ? issue.getAssignee().getDisplayName()
                : "Unassigned";

        Date dueDate = issue.getDueDate();

        return new IssueViewDTO(key, summary, status, assignee, dueDate);
    }

    /**
     * Получить задачу по уникальному ключу (например, "TEST-1")
     * @param issueKey уникальный ключ задачи
     * @return задача в формате DTO или null, если не найдена
     */
    @Override
    public IssueViewDTO getIssueByKey(String issueKey) {
        //проверяем, что ключ не пустой
        if (issueKey == null || issueKey.isBlank()) {
            return null;
        }

        //формируем JQL-запрос для поиска задачи по ключу
        String jql = "issue = \"" + issueKey + "\"";

        try {
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

            if (user == null) {
                return null;
            }

            SearchService.ParseResult parseResult = _searchService.parseQuery(user, jql);

            if (!parseResult.isValid()) {
                return null;
            }

            Query query = parseResult.getQuery();

            SearchResults<Issue> results = _searchService.search(
                    user,
                    query,
                    com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter()
            );

            if (results.getResults().isEmpty()) {
                return null;
            }

            Issue issue = results.getResults().get(0);
            String projectKey = issue.getProjectObject().getKey();

            if (!_visibilityService.canUserSeeIssue(projectKey, issue, user)) {
                return null;
            }

            return mapToDTO(issue);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
