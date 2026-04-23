package com.projmodel.plugin.dto;

import java.util.Date;

/**
 * DTO для отображения главных данных о задаче у пользователя
 */
public class IssueViewDTO {

    private String _key;
    private String _summary;
    private String _status;
    private String _assignee;
    private Date _dueDate;

    public IssueViewDTO(String key, String summary, String status, String assignee, Date dueDate) {
        _key = key;
        _summary = summary;
        _status = status;
        _assignee = assignee;
        _dueDate = dueDate;
    }

    public String get_key(){
        return _key;
    }

    public String get_summary() {
        return _summary;
    }

    public String get_status() {
        return _status;
    }

    public String getAssignee() {
        return _assignee;
    }

    public Date getDueDate() {
        return _dueDate;
    }
}
