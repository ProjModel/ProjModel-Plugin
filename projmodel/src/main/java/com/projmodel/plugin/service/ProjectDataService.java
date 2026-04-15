package com.projmodel.plugin.service;

import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * Интерфейс для реализации чтения данных из проектов Jira (способность в целом читать данные; будет реализовано через Jira API)
 */
public interface ProjectDataService {

    /**
     * Получить все проекты, которые видит Jira
     * @return список видимых проектов
     */
    List<Project> getAllProjects();

    /**
     * Получить конкретный проект по уникальному ключу
     * @param projectKey уникальный ключ проекта
     * @return нужный проект
     */
    Project getProjectByKey(String projectKey);
}
