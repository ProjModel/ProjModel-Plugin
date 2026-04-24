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

    /**
     * Создать запрос на генерацию отчёта и сохранить его в БД
     * @param projectKey ключ проекта, для которого создаётся отчёт
     * @param issueKeys список ключей задач для включения в отчёт
     * @param format формат отчёта ("WORD" или "PDF")
     * @return созданная запись в БД со статусом PENDING
     * @throws IllegalArgumentException если входные данные некорректны
     */
    @Override
    public ReportTaskAO createReportRequest(String projectKey, List<String> issueKeys, String format) {
        //валидация входных данных
        if (projectKey == null || projectKey.isBlank()) {
            throw new IllegalArgumentException("Ключ проекта не может быть пустым");
        }
        if (issueKeys == null || issueKeys.isEmpty()) {
            throw new IllegalArgumentException("Список задач не может быть пустым");
        }
        if (format == null || (!format.equals("WORD") && !format.equals("PDF"))) {
            throw new IllegalArgumentException("Формат должен быть WORD или PDF");
        }

        //создаем новую запись в БД
        ReportTaskAO report = _activeObjects.create(ReportTaskAO.class);

        //заполняем данные запроса
        report.setProjectKey(projectKey);

        //сохраняем ключи задач через запятую для удобства хранения
        report.setIssueKeys(String.join(",", issueKeys));

        report.setReportFormat(format);
        report.setCreatedDate(new Date());
        report.setStatus("PENDING");  //начальный статус - ожидает генерации
        report.setFilePath(null);     //файл пока не сгенерирован

        //сохраняем запись в БД
        report.save();

        return report;
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
