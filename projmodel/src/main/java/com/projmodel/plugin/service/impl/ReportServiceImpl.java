package com.projmodel.plugin.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.report.ReportGenerator;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.ReportService;
import net.java.ao.Query;
import java.util.HashMap;
import java.util.Map;

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

        // Пробуем через прямое создание с параметрами
        Map<String, Object> params = new HashMap<>();
        params.put("PROJECT_KEY", projectKey);
        params.put("ISSUE_KEYS", String.join(",", issueKeys));
        params.put("REPORT_FORMAT", format);
        params.put("CREATED_DATE", new Date());
        params.put("STATUS", "PENDING");
        params.put("FILE_PATH", null);

        ReportTaskAO report = _activeObjects.create(ReportTaskAO.class, params);
        report.save();

        return report;
    }

    /**
     * Получить все запросы на отчёты для указанного проекта
     * @param projectKey ключ проекта
     * @return список запросов, отсортированный по дате создания (новые сверху)
     */
    @Override
    public List<ReportTaskAO> getReportsByProject(String projectKey) {
        //выполняем запрос к БД: ищем все отчёты проекта, сортируем по дате создания
        ReportTaskAO[] reports = _activeObjects.find(
                ReportTaskAO.class,
                Query.select()
                        .where("PROJECT_KEY = ?", projectKey)
                        .order("CREATED_DATE DESC")
        );

        return Arrays.asList(reports);
    }

    /**
     * Получить конкретный запрос на отчёт по его ID
     * @param id идентификатор записи в БД
     * @return запись запроса на отчёт или null, если не найдена
     */
    @Override
    public ReportTaskAO getReportById(int id) {
        return _activeObjects.get(ReportTaskAO.class, id);
    }

    /**
     * Обновить статус запроса на отчёт
     * @param reportId идентификатор записи
     * @param status новый статус ("PENDING", "GENERATED", "ERROR")
     * @param filePath путь к сгенерированному файлу (может быть null)
     */
    @Override
    public void updateReportStatus(int reportId, String status, String filePath) {
//получаем запись из БД по ID
        ReportTaskAO report = _activeObjects.get(ReportTaskAO.class, reportId);

        if (report != null) {
            //обновляем статус
            report.setStatus(status);

            //если передан путь к файлу, сохраняем его
            if (filePath != null && !filePath.isBlank()) {
                report.setFilePath(filePath);
            }

            //сохраняем изменения в БД
            report.save();
        }
    }

    /**
     * Сгенерировать файл отчёта (HTML или текст) по запросу
     * Получает задачи по ключам, генерирует файл и обновляет статус запроса
     * @param reportId идентификатор запроса на отчёт
     * @return массив байтов сгенерированного файла
     * @throws RuntimeException если отчёт не найден или произошла ошибка генерации
     */
    @Override
    public byte[] generateReportFile(int reportId) {
        //получаем запрос на отчёт из БД
        ReportTaskAO report = getReportById(reportId);

        if (report == null) {
            throw new IllegalArgumentException("Отчёт не найден: " + reportId);
        }

        //преобразуем строку ключей в список (ключи хранятся через запятую)
        List<String> issueKeys = Arrays.asList(report.getIssueKeys().split(","));

        //получаем полные данные задач по их ключам
        List<IssueViewDTO> issues = new ArrayList<>();
        for (String key : issueKeys) {
            IssueViewDTO issue = _issueDataService.getIssueByKey(key.trim());
            if (issue != null) {
                issues.add(issue);
            }
        }

        try {
            byte[] fileData;

            //генерируем файл в зависимости от формата
            if ("WORD".equals(report.getReportFormat())) {
                // HTML открывается в Word как обычный документ
                fileData = ReportGenerator.generateHtmlReport(issues, report.getProjectKey());
            } else {
                // Текстовый отчёт (можно сохранить как PDF через браузер)
                fileData = ReportGenerator.generateTextReport(issues, report.getProjectKey());
            }

            //обновляем статус на "сгенерирован"
            updateReportStatus(reportId, "GENERATED", null);

            return fileData;

        } catch (Exception e) {
            //в случае ошибки обновляем статус и выбрасываем исключение
            updateReportStatus(reportId, "ERROR", null);
            throw new RuntimeException("Ошибка при генерации отчёта: " + e.getMessage(), e);
        }
    }
}
