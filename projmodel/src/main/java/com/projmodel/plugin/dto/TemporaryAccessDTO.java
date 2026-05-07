package com.projmodel.plugin.dto;

import java.util.Date;

public class TemporaryAccessDTO {

    private final String projectKey;
    private final String issueKey;
    private final String username;
    private final Date expiresAt;

    public TemporaryAccessDTO(String projectKey, String issueKey, String username, Date expiresAt) {
        this.projectKey = projectKey;
        this.issueKey = issueKey;
        this.username = username;
        this.expiresAt = expiresAt;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getUsername() {
        return username;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
