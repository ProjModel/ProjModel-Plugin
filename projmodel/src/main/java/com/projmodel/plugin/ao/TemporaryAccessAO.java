package com.projmodel.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

import java.util.Date;

@Preload
public interface TemporaryAccessAO extends Entity {

    @NotNull
    String getProjectKey();
    void setProjectKey(String projectKey);

    @NotNull
    String getIssueKey();
    void setIssueKey(String issueKey);

    @NotNull
    String getUsername();
    void setUsername(String username);

    @NotNull
    Date getExpiresAt();
    void setExpiresAt(Date expiresAt);
}
