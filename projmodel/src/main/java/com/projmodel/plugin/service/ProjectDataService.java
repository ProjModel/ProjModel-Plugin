package com.projmodel.plugin.service;

import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * Интерфейс для реализации чтения данных из проектов Jira (способность в целом читать данные; будет реализовано через Jira API)
 */
public interface ProjectDataService {

    List<Project> getAllProjects();
    Project getProjectByKey(String projectKey);
}
