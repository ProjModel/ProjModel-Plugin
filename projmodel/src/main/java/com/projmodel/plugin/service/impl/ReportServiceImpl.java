package com.projmodel.plugin.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.report.ReportGenerator;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.ReportService;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Сервис по работе с отчётами
 * Отвечает за создание запросов на отчёты, их хранение в БД и генерацию файлов
 */
@Named
public class ReportServiceImpl implements ReportService {

    /**
     * Компонент Active Objects для работы с базой данных плагина
     */
    private final ActiveObjects _activeObjects;

    /**
     * Сервис для получения данных о задачах из Jira
     */
    private final IssueDataService _issueDataService;

    /**
     * Конструктор сервиса
     * @param activeObjects компонент для работы с БД плагина
     * @param issueDataService сервис для получения задач
     */
    @Inject
    public ReportServiceImpl(@ComponentImport ActiveObjects activeObjects,
                             IssueDataService issueDataService) {
        _activeObjects = activeObjects;
        _issueDataService = issueDataService;
    }

    @Override
    public ReportTaskAO createReportRequest(String projectKey, List<String> issueKeys, String format) {
        return null;
    }

    @Override
    public List<ReportTaskAO> getReportsByProject(String projectKey) {
        return List.of();
    }

    @Override
    public ReportTaskAO getReportById(int id) {
        return null;
    }

    @Override
    public void updateReportStatus(int reportId, String status, String filePath) {

    }

    @Override
    public byte[] generateReportFile(int reportId) {
        return new byte[0];
    }
}
