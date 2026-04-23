package com.projmodel.plugin.service;

import com.atlassian.jira.issue.Issue;
import com.projmodel.plugin.dto.IssueViewDTO;

import java.util.List;

/**
 * Интерфейс для реализации чтения данных из задач проектов Jira (само чтение)
 */
public interface IssueDataService {

    /**
     * Получить список задач проекта по уникальному ключу проекта (например, проект "TEST")
     * @param projectKey уникальный ключ проекта
     * @return список задач проекта в формате DTO
     */
    List<IssueViewDTO> getIssuesForProject(String projectKey);

    /**
     * Получить список незавершенных задач проекта по уникальному ключу проекта
     * @param projectKey уникальный ключ проекта
     * @return список незавершенных задач проекта в формате DTO
     */
    List<IssueViewDTO> getOpenIssuesForProject(String projectKey);
}
