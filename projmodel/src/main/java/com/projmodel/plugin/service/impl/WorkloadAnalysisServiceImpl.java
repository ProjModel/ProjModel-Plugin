package com.projmodel.plugin.service.impl;

import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.WorkloadAnalysisService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис по анализу загрузки участников команды на основе незавершенных задач проекта
 */
public class WorkloadAnalysisServiceImpl implements WorkloadAnalysisService {

    /**
     * Сервис для получения данных о задачах из Jira
     */
    private final IssueDataService _issueDataService;

    /**
     * Конструктор сервиса
     * @param issueDataService сервис по работе с задачами
     */
    @Inject
    public WorkloadAnalysisServiceImpl(IssueDataService issueDataService) {
        _issueDataService = issueDataService;
    }

    @Override
    public List<WorkloadViewDTO> analyzeWorkload(String projectKey) {
        return List.of();
    }
}
