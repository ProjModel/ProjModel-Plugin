package com.projmodel.plugin.service.impl;

import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.service.VisibilityAuditService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Date;

public class VisibilityAuditServiceImpl implements VisibilityAuditService {

    private static final Logger log = LoggerFactory.getLogger(VisibilityAuditServiceImpl.class);

    @Override
    public void logVisibilityChange(ApplicationUser author, String message) {
        String username = author != null ? author.getName() : "unknown";

        log.info("[ProjModel Visibility Audit] author={}, time={}, action={}",
                username,
                new Date(),
                message
        );
    }
}
