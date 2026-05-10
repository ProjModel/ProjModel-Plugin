package com.projmodel.plugin.service.impl;

import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.service.DeadlineAnalysisService;
import com.projmodel.plugin.service.IssueDataService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class DeadlineAnalysisServiceImpl implements DeadlineAnalysisService {

    private final IssueDataService issueDataService;

    @Inject
    public DeadlineAnalysisServiceImpl(IssueDataService issueDataService) {
        this.issueDataService = issueDataService;
    }

    @Override
    public List<DeadlineIssueDTO> analyzeDeadlines(String projectKey) {
        List<IssueViewDTO> openIssues = issueDataService.getOpenIssuesForProject(projectKey);

        if (openIssues == null || openIssues.isEmpty()) {
            return Collections.emptyList();
        }
        Date now = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfToday = cal.getTime();

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 3);
        Date threeDaysLater = cal.getTime();

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date sevenDaysLater = cal.getTime();

        List<DeadlineIssueDTO> result = new ArrayList<>();

        for (IssueViewDTO issue : openIssues) {
            String riskLevel = calculateRiskLevel(issue.getDueDate(), now, endOfToday,
                    threeDaysLater, sevenDaysLater);

            result.add(new DeadlineIssueDTO(
                    issue.get_key(),
                    issue.get_summary(),
                    issue.get_status(),
                    issue.getAssignee(),
                    issue.getDueDate(),
                    riskLevel
            ));
        }

        result.sort(Comparator.comparingInt(dto -> {
            switch (dto.getRiskLevel()) {
                case "CRITICAL": return 0;
                case "HIGH": return 1;
                case "MEDIUM": return 2;
                case "LOW": return 3;
                case "NO_DEADLINE": return 4;
                default: return 5;
            }
        }));

        return result;
    }

    /**
     * Рассчитать уровень риска по дедлайну
     *
     * @param dueDate дедлайн задачи (может быть null)
     * @param now текущее время
     * @param endOfToday конец текущего дня (23:59:59)
     * @param threeDaysLater дата через 3 дня
     * @param sevenDaysLater дата через 7 дней
     * @return уровень риска: CRITICAL, HIGH, MEDIUM, LOW или NO_DEADLINE
     */
    private String calculateRiskLevel(Date dueDate, Date now, Date endOfToday,
                                      Date threeDaysLater, Date sevenDaysLater) {
        // Правило 1: дедлайн отсутствует
        if (dueDate == null) {
            return "NO_DEADLINE";
        }

        if (dueDate.before(now) || (dueDate.before(endOfToday) || dueDate.equals(endOfToday))) {
            return "CRITICAL";
        }

        if (dueDate.before(threeDaysLater)) {
            return "HIGH";
        }
        
        if (dueDate.before(sevenDaysLater)) {
            return "MEDIUM";
        }

        return "LOW";
    }
}