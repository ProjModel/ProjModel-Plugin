package com.projmodel.plugin.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.projmodel.plugin.ao.TemporaryAccessAO;
import com.projmodel.plugin.ao.VisibilityRuleAO;
import com.projmodel.plugin.dto.VisibilityRuleDTO;
import com.projmodel.plugin.service.VisibilityAuditService;
import com.projmodel.plugin.service.VisibilityService;
import net.java.ao.DBParam;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class VisibilityServiceImpl implements VisibilityService {

    private final ActiveObjects _ao;

    private final VisibilityAuditService _auditService;

    private boolean _enabled = true;

    @Inject
    public VisibilityServiceImpl(@ComponentImport ActiveObjects ao, VisibilityAuditService auditService) {

        _ao = ao;
        _auditService = auditService;
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

        if(!_enabled) {
            return true;
        }

        if (isTeamLead(user)) {
            return true;
        }

        if (hasTemporaryAccess(projectKey, issue.getKey(), user.getName())) {
            return true;
        }

        Set<String> issueLabels = issue.getLabels().stream()
                .map(Label::getLabel)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        String username = user.getName().toLowerCase();

        Set<String> usernameParts = Arrays.stream(username.split("[_\\-.]"))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toSet());

        for (String label : issueLabels) {
            if (usernameParts.contains(label)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<VisibilityRuleDTO> getRulesForProject(String projectKey) {

        createDefaultRulesIfNeeded(projectKey);

        if(projectKey == null || projectKey.isBlank()) {
            return Collections.emptyList();
        }

        VisibilityRuleAO[] rules = _ao.find(
                VisibilityRuleAO.class,
                Query.select().where("PROJECT_KEY = ?", projectKey)
        );

        return Arrays.stream(rules)
                .map(this::mapRuleToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveRule(VisibilityRuleDTO rule, ApplicationUser author) {
        if (rule == null) {
            return;
        }

        if (rule.getProjectKey() == null || rule.getProjectKey().isBlank()
                || rule.getRoleName() == null || rule.getRoleName().isBlank()
                || rule.getAllowedLabels() == null
                || rule.getAllowedLabels().isEmpty()) {

            return;
        }

        _ao.executeInTransaction(() -> {
            VisibilityRuleAO[] existingRules = _ao.find(
                    VisibilityRuleAO.class,
                    Query.select().where(
                            "PROJECT_KEY = ? AND ROLE_NAME = ?",
                            rule.getProjectKey(),
                            rule.getRoleName()
                    )
            );

            VisibilityRuleAO ruleAO;

            if (existingRules.length > 0) {
                ruleAO = existingRules[0];
            } else {
                ruleAO = _ao.create(
                        VisibilityRuleAO.class,
                        new DBParam("PROJECT_KEY", rule.getProjectKey()),
                        new DBParam("ROLE_NAME", rule.getRoleName()),
                        new DBParam("ALLOWED_LABELS", String.join(",", rule.getAllowedLabels()))
                );
            }

            ruleAO.setProjectKey(rule.getProjectKey());
            ruleAO.setRoleName(rule.getRoleName());
            ruleAO.setAllowedLabels(String.join(",", rule.getAllowedLabels()));
            ruleAO.setEnabled(rule.isEnabled());
            ruleAO.save();

            return null;
        });

        _auditService.logVisibilityChange(author,
                "Saved rule: project=" + rule.getProjectKey()
                        + ", role=" + rule.getRoleName()
                        + ", labels=" + rule.getAllowedLabels()
                        + ", enabled=" + rule.isEnabled()
        );
    }

    @Override
    public void grantTemporaryAccess(String projectKey, String issueKey, String username, int hours, ApplicationUser author) {
        if (projectKey == null || projectKey.isBlank()
                || issueKey == null || issueKey.isBlank()
                || username == null || username.isBlank()) {

            return;
        }

        if (hours < 1 || hours > 720) {
            throw new IllegalArgumentException("Temporary access must be from 1 hour to 30 days");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, hours);
        Date expiresAt = calendar.getTime();

        _ao.executeInTransaction(() -> {
            TemporaryAccessAO access = _ao.create(TemporaryAccessAO.class,
                    new DBParam("PROJECT_KEY", projectKey),
                    new DBParam("ISSUE_KEY", issueKey),
                    new DBParam("USERNAME", username),
                    new DBParam("EXPIRES_AT", expiresAt)
                    );
            access.save();

            return null;
        });

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

        TemporaryAccessAO[] accesses = _ao.find(
                TemporaryAccessAO.class,
                Query.select().where(
                        "PROJECT_KEY = ? AND ISSUE_KEY = ? AND USERNAME = ?",
                        projectKey,
                        issueKey,
                        username
                )
        );

        for (TemporaryAccessAO access : accesses) {
            if (access.getExpiresAt().before(now)) {
                _ao.delete(access);
                continue;
            }

            return true;
        }

        return false;
    }

    private VisibilityRuleDTO mapRuleToDTO(VisibilityRuleAO ruleAO) {
        List<String> labels = Arrays.stream(ruleAO.getAllowedLabels().split(","))
                .map(String::trim)
                .filter(label -> !label.isEmpty())
                .collect(Collectors.toList());

        return new VisibilityRuleDTO(
                ruleAO.getProjectKey(),
                ruleAO.getRoleName(),
                labels,
                ruleAO.isEnabled()
        );
    }

    private void createDefaultRulesIfNeeded(String projectKey) {
        if(projectKey == null || projectKey.isBlank()) {
            return;
        }

        VisibilityRuleAO[] existingRules = _ao.find(
                VisibilityRuleAO.class,
                Query.select().where("PROJECT_KEY = ?", projectKey)
        );

        if (existingRules.length > 0) {
            return;
        }

        saveDefaultRule(projectKey, "frontend", Arrays.asList("frontend", "ui"));
        saveDefaultRule(projectKey, "backend", Arrays.asList("backend", "api"));
        saveDefaultRule(projectKey, "tester", Arrays.asList("test", "qa"));
        saveDefaultRule(projectKey, "designer", Arrays.asList("design", "des"));
    }

    private void saveDefaultRule(String projectKey, String roleName, List<String> labels) {
        if(projectKey == null || roleName == null || labels.isEmpty()) {
            return;
        }

        String labelsStr = String.join(",", labels); // раньше было labels.toString();
        _ao.executeInTransaction(() -> {
            VisibilityRuleAO ruleAO = _ao.create(VisibilityRuleAO.class,
                    new DBParam("PROJECT_KEY", projectKey),
                    new DBParam("ROLE_NAME", roleName),
                    new DBParam("ALLOWED_LABELS", labelsStr));
            ruleAO.setEnabled(true);
            ruleAO.save();

            return null;
        });
    }

    private boolean isTeamLead(ApplicationUser user) {
        String username = user.getName().toLowerCase();

        return username.contains("admin")
                || username.contains("lead")
                || username.contains("teamlead");
    }
}
