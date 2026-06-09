package com.projmodel.plugin.service;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.dto.VisibilityRuleDTO;

import java.util.List;

public interface VisibilityService {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    List<Issue> filterIssues(String projectKey, List<Issue> issues, ApplicationUser user);

    boolean canUserSeeIssue(String projectKey, Issue issue, ApplicationUser user);

    List<VisibilityRuleDTO> getRulesForProject(String projectKey);

    void saveRule(VisibilityRuleDTO rule, ApplicationUser author);

    void grantTemporaryAccess(String projectKey, String issueKey, String username, int hours, ApplicationUser author);

}
