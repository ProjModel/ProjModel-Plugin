package com.projmodel.plugin.service.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.dto.TemporaryAccessDTO;
import com.projmodel.plugin.dto.VisibilityRuleDTO;
import com.projmodel.plugin.service.VisibilityAuditService;
import com.projmodel.plugin.service.VisibilityService;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class VisibilityServiceImpl implements VisibilityService {


    private final VisibilityAuditService _auditService;

    private boolean _enabled = true;

    private final List<VisibilityRuleDTO> _rules = new ArrayList<>();
    private final List<TemporaryAccessDTO> _temporaryAccesses = new ArrayList<>();

    @Inject
    public VisibilityServiceImpl(VisibilityAuditService auditService) {
        _auditService = auditService;

        // MVP-правила. Потом перенесём в Active Objects.
        _rules.add(new VisibilityRuleDTO("TEST", "frontend", Arrays.asList("frontend", "ui"), true));
        _rules.add(new VisibilityRuleDTO("TEST", "backend", Arrays.asList("backend", "api"), true));
        _rules.add(new VisibilityRuleDTO("TEST", "tester", Arrays.asList("test", "qa"), true));
    }

    // ----- override methods from interface ----- //

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    @Override
    public List<Issue> filterIssues(String projectKey, List<Issue> issues, ApplicationUser user) {
        if (!_enabled) {
            return issues;
        }

        if (user == null) {
            return Collections.emptyList();
        }

        if (isTeamLead(user)) {
            return issues;
        }

        return issues.stream()
                .filter(issue -> canUserSeeIssue(projectKey, issue, user))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUserSeeIssue(String projectKey, Issue issue, ApplicationUser user) {
        if (issue == null || user == null) {
            return false;
        }

        if (isTeamLead(user)) {
            return true;
        }

        if (hasTemporaryAccess(projectKey, issue.getKey(), user.getName())) {
            return true;
        }

        String userRole = getUserRole(user);

        Optional<VisibilityRuleDTO> ruleOptional = getRulesForProject(projectKey).stream()
                .filter(VisibilityRuleDTO::isEnabled)
                .filter(rule -> rule.getRoleName().equalsIgnoreCase(userRole))
                .findFirst();

        if (!ruleOptional.isPresent()) {
            return false;
        }

        VisibilityRuleDTO rule = ruleOptional.get();
        Set<String> labels = issue.getLabels().stream()
                .map(Label::getLabel)
                .collect(Collectors.toSet());;

        return rule.getAllowedLabels().stream()
                .anyMatch(labels::contains);
    }

    @Override
    public List<VisibilityRuleDTO> getRulesForProject(String projectKey) {
        return _rules.stream()
                .filter(rule -> rule.getProjectKey().equalsIgnoreCase(projectKey))
                .collect(Collectors.toList());
    }

    @Override
    public void saveRule(VisibilityRuleDTO rule, ApplicationUser author) {
        _rules.removeIf(existing ->
                existing.getProjectKey().equalsIgnoreCase(rule.getProjectKey())
                        && existing.getRoleName().equalsIgnoreCase(rule.getRoleName())
        );

        _rules.add(rule);

        _auditService.logVisibilityChange(author,
                "Saved rule: project=" + rule.getProjectKey()
                        + ", role=" + rule.getRoleName()
                        + ", labels=" + rule.getAllowedLabels()
                        + ", enabled=" + rule.isEnabled()
        );
    }

    @Override
    public void grantTemporaryAccess(String projectKey, String issueKey, String username, int hours, ApplicationUser author) {
        if (hours < 1 || hours > 720) {
            throw new IllegalArgumentException("Temporary access must be from 1 hour to 30 days");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, hours);

        TemporaryAccessDTO access = new TemporaryAccessDTO(projectKey, issueKey, username, calendar.getTime());
        _temporaryAccesses.add(access);

        _auditService.logVisibilityChange(author,
                "Temporary access granted: project=" + projectKey
                        + ", issue=" + issueKey
                        + ", user=" + username
                        + ", hours=" + hours
        );
    }

    // ----- helping methods ----- //

    private boolean hasTemporaryAccess(String projectKey, String issueKey, String username) {
        Date now = new Date();

        _temporaryAccesses.removeIf(access -> access.getExpiresAt().before(now));

        return _temporaryAccesses.stream()
                .anyMatch(access ->
                        access.getProjectKey().equalsIgnoreCase(projectKey)
                                && access.getIssueKey().equalsIgnoreCase(issueKey)
                                && access.getUsername().equalsIgnoreCase(username)
                                && access.getExpiresAt().after(now)
                );
    }

    private boolean isTeamLead(ApplicationUser user) {
        String username = user.getName().toLowerCase();

        return username.contains("admin")
                || username.contains("lead")
                || username.contains("teamlead");
    }

    private String getUserRole(ApplicationUser user) {
        String username = user.getName().toLowerCase();

        if (username.contains("front")) {
            return "frontend";
        }

        if (username.contains("back")) {
            return "backend";
        }

        if (username.contains("test")) {
            return "tester";
        }

        return "member";
    }

}
