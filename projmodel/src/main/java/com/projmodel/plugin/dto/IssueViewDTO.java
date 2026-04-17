package com.projmodel.plugin.dto;

import java.util.Date;

/**
 * DTO для отображения главных данных о задаче у пользователя
 */
public class IssueViewDTO {

    private String key;
    private String summary;
    private String status;
    private String assignee;
    private Date dueDate;

    public IssueViewDTO(String key, String summary, String status, String assignee, Date dueDate) {
        this.key = key;
        this.summary = summary;
        this.status = status;
        this.assignee = assignee;
        this.dueDate = dueDate;
    }
}
