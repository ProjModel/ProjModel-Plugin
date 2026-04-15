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

@Named
public class ProjectDataServiceImpl implements ProjectDataService
{
    private final ProjectManager _projectManager;

    public ProjectDataServiceImpl(ProjectManager manager) {
        _projectManager = manager;
    }

    @Override
    public List<Project> getAllProjects() {
        return new ArrayList<>(_projectManager.getProjectObjects());
    }

    @Override
    public Project getProjectByKey(String projectKey) {
        if(projectKey == null || projectKey.isBlank()) {
            return null;
        }

        return _projectManager.getProjectByCurrentKey(projectKey);
    }
}