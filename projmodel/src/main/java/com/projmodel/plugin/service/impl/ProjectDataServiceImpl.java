package com.projmodel.plugin.service.impl;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.projmodel.plugin.api.MyPluginComponent;
import com.projmodel.plugin.service.ProjectDataService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class ProjectDataServiceImpl implements ProjectDataService
{
    @Override
    public List<Project> getAllProjects() {
        return List.of();
    }

    @Override
    public Project getProjectByKey(String projectKey) {
        return null;
    }
}