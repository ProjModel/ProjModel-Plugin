package com.projmodel.plugin.service;

import com.projmodel.plugin.ao.ReportTaskAO;
import java.util.List;

/**
 * Интерфейс сервиса для работы с отчётами
 * Предоставляет методы для создания запросов на отчёты и их генерации
 */

public interface ReportService {
    /**
     * Создать запрос на генерацию отчёта и сохранить его в БД
     * @param projectKey ключ проекта, для которого создаётся отчёт
     * @param issueKeys список ключей задач для включения в отчёт
     * @param format формат отчёта ("WORD" или "PDF")
     * @return созданная запись в БД со статусом PENDING
     */
    ReportTaskAO createReportRequest(String projectKey, List<String> issueKeys, String format);

    /**
     * Получить все запросы на отчёты для указанного проекта
     * @param projectKey ключ проекта
     * @return список запросов на отчёты, отсортированный по дате создания (новые сверху)
     */
    List<ReportTaskAO> getReportsByProject(String projectKey);

    /**
     * Получить конкретный запрос на отчёт по его ID
     * @param id идентификатор записи в БД
     * @return запись запроса на отчёт или null, если не найдена
     */
    ReportTaskAO getReportById(int id);

    /**
     * Обновить статус запроса на отчёт
     * @param reportId идентификатор записи
     * @param status новый статус ("PENDING", "GENERATED", "ERROR")
     * @param filePath путь к сгенерированному файлу (опционально, может быть null)
     */
    void updateReportStatus(int reportId, String status, String filePath);

    /**
     * Сгенерировать файл отчёта (Word или PDF) по запросу
     * @param reportId идентификатор запроса на отчёт
     * @return массив байтов сгенерированного файла
     * @throws RuntimeException если отчёт не найден или произошла ошибка генерации
     */
    byte[] generateReportFile(int reportId);
}
