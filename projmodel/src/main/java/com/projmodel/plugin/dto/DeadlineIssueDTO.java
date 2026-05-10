package com.projmodel.plugin.dto;

import java.util.Date;

public class DeadlineIssueDTO {
    private final String issueKey;
    private final String summary;
    private final String status;
    private final String assignee;
    private final Date dueDate;
    private final String riskLevel;  // CRITICAL, HIGH, MEDIUM, LOW, NO_DEADLINE

    public DeadlineIssueDTO(String issueKey, String summary, String status,
                            String assignee, Date dueDate, String riskLevel) {
        this.issueKey = issueKey;
        this.summary = summary;
        this.status = status;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.riskLevel = riskLevel;
    }

    public String getIssueKey() { return issueKey; }
    public String getSummary() { return summary; }
    public String getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public Date getDueDate() { return dueDate; }
    public String getRiskLevel() { return riskLevel; }

    // Вспомогательные методы для шаблона Velocity
    public String getRiskBadge() {
        switch (riskLevel) {
            case "CRITICAL": return "critical";
            case "HIGH": return "high";
            case "MEDIUM": return "medium";
            case "LOW": return "low";
            default: return "low";
        }
    }

    public String getRiskText() {
        switch (riskLevel) {
            case "CRITICAL": return "Критический";
            case "HIGH": return "Высокий";
            case "MEDIUM": return "Средний";
            case "LOW": return "Низкий";
            case "NO_DEADLINE": return "Без дедлайна";
            default: return "Неизвестно";
        }
    }

    public String getRowClass() {
        switch (riskLevel) {
            case "CRITICAL": return "risk-critical";
            case "HIGH": return "risk-high";
            case "MEDIUM": return "risk-medium";
            default: return "risk-low";
        }
    }
}
