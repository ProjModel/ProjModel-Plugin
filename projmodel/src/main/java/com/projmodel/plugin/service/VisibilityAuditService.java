package com.projmodel.plugin.service;

import com.atlassian.jira.user.ApplicationUser;

public interface VisibilityAuditService {

    void logVisibilityChange(ApplicationUser author, String message);
}
