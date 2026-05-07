package com.projmodel.plugin.dto;

import java.util.List;

public class VisibilityRuleDTO {

    private final String projectKey;
    private final String roleName;
    private final List<String> allowedLabels;
    private final boolean enabled;

    public VisibilityRuleDTO(String projectKey, String roleName, List<String> allowedLabels, boolean enabled) {
        this.projectKey = projectKey;
        this.roleName = roleName;
        this.allowedLabels = allowedLabels;
        this.enabled = enabled;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getAllowedLabels() {
        return allowedLabels;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
