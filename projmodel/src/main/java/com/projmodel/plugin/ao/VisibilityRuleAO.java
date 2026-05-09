package com.projmodel.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.NotNull;

@Preload
public interface VisibilityRuleAO extends Entity {
    @NotNull
    String getProjectKey();
    void setProjectKey(String projectKey);

    @NotNull
    String getRoleName();
    void setRoleName(String roleName);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getAllowedLabels();
    void setAllowedLabels(String allowedLabels);

    boolean isEnabled();
    void setEnabled(boolean enabled);

}
