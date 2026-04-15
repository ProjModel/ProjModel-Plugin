package com.projmodel.plugin.service.impl;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.projmodel.plugin.api.MyPluginComponent;
import com.projmodel.plugin.service.ProjectDataService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис по обработке проектов в Jira (предоставляет доступ к проектам в Jira)
 */
@Named
public class ProjectDataServiceImpl implements ProjectDataService
{
    /**
     * Jira-компонент, менеджер, умеющий работать с проектами
     */
    private final ProjectManager _projectManager;

    /**
     * Конструктор сервиса
     * @param manager компонент по работе с проектами
     */
    @Inject
    public ProjectDataServiceImpl(@ComponentImport ProjectManager manager) {
        _projectManager = manager;
    }

    /**
     * Получить все проекты, которые видит Jira
     * @return список видимых проектов
     */
    @Override
    public List<Project> getAllProjects() {
        return new ArrayList<>(_projectManager.getProjectObjects());
    }

    /**
     * Получить конкретный проект по уникальному ключу
     * @param projectKey уникальный ключ проекта
     * @return нужный проект
     */
    @Override
    public Project getProjectByKey(String projectKey) {
        if(projectKey == null || projectKey.isBlank()) {
            return null;
        }

        return _projectManager.getProjectByCurrentKey(projectKey);
    }
}