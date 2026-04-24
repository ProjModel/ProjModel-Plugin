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
 * Сервис по анализу загрузки участников команды на основе незавершенных задач проекта v1
 */
@Named
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

    /**
     * Проанализировать загрузку участников по всем незавершенным задачам проекта
     * @param projectKey уникальный ключ проекта
     * @return список результатов анализа загрузки по каждому исполнителю
     */
    @Override
    public List<WorkloadViewDTO> analyzeWorkload(String projectKey)
    {
        //получаем все незавершенные задачи проекта
        List<IssueViewDTO> openIssues = _issueDataService.getOpenIssuesForProject(projectKey);

        //если задач нет или проект не найден, возвращаем пустой список
        if (openIssues == null || openIssues.isEmpty()) {
            return Collections.emptyList();
        }

        //группируем задачи по исполнителю (assignee)
        Map<String, List<IssueViewDTO>> groupedByAssignee = openIssues.stream()
                .collect(Collectors.groupingBy(IssueViewDTO::getAssignee));

        List<WorkloadViewDTO> result = new ArrayList<>();

        //текущая дата для сравнения с дедлайнами
        Date now = new Date();

        //дата через 7 дней от текущей (для подсчета задач с близким дедлайном)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Date sevenDaysLater = calendar.getTime();

        //обходим каждого исполнителя и считаем его показатели загрузки
        for (Map.Entry<String, List<IssueViewDTO>> entry : groupedByAssignee.entrySet()) {
            String assignee = entry.getKey();
            List<IssueViewDTO> issues = entry.getValue();

            int totalTasks = issues.size();
            int tasksDueWithin7Days = 0;
            int overdueTasks = 0;
            int tasksWithoutDueDate = 0;

            //проходим по всем задачам исполнителя и классифицируем по дедлайнам
            for (IssueViewDTO issue : issues) {
                Date dueDate = issue.getDueDate();

                if (dueDate == null) {
                    //задача без дедлайна
                    tasksWithoutDueDate++;
                } else if (dueDate.before(now)) {
                    //дедлайн уже прошел — задача просрочена
                    overdueTasks++;
                } else if (dueDate.before(sevenDaysLater)) {
                    //дедлайн в ближайшие 7 дней
                    tasksDueWithin7Days++;
                }
                //остальные задачи (дедлайн больше чем через 7 дней) не учитываем в специальных счетчиках
            }

            //вычисляем уровень загрузки на основе полученных показателей
            String loadLevel = calculateLoadLevel(totalTasks, overdueTasks, tasksDueWithin7Days);

            //создаем DTO с результатами анализа для данного исполнителя
            result.add(new WorkloadViewDTO(
                    assignee,
                    totalTasks,
                    tasksDueWithin7Days,
                    overdueTasks,
                    tasksWithoutDueDate,
                    loadLevel
            ));
        }

        return result;
    }

    /**
     * Вычислить уровень нагрузки исполнителя на основе количества задач и их срочности
     * @param totalTasks общее количество незавершенных задач
     * @param overdueTasks количество просроченных задач
     * @param tasksDueWithin7Days количество задач с дедлайном в ближайшие 7 дней
     * @return уровень нагрузки: "low", "medium", "high" или "critical"
     */
    private String calculateLoadLevel(int totalTasks, int overdueTasks, int tasksDueWithin7Days)
    {
        //критическая нагрузка: больше 2 просроченных задач или больше 10 задач всего
        if (overdueTasks > 2 || totalTasks > 10) {
            return "critical";
        }
        //высокая нагрузка: есть просроченные задачи, или больше 3 срочных задач, или всего больше 7 задач
        else if (overdueTasks > 0 || tasksDueWithin7Days > 3 || totalTasks > 7) {
            return "high";
        }
        //средняя нагрузка: больше 1 задачи с близким дедлайном или всего больше 4 задач
        else if (tasksDueWithin7Days > 1 || totalTasks > 4) {
            return "medium";
        }
        //низкая нагрузка: все остальные случаи
        else {
            return "low";
        }
    }
}
