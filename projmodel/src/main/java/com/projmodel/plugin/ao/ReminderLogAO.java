package com.projmodel.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import java.util.Date;

@Preload
public interface ReminderLogAO extends Entity{
    @NotNull
    String getIssueKey();
    void setIssueKey(String issueKey);

    @NotNull
    String getProjectKey();
    void setProjectKey(String projectKey);

    @NotNull
    String getAssignee();
    void setAssignee(String assignee);

    Date getDueDate();
    void setDueDate(Date dueDate);

    @NotNull
    Date getReminderDate();
    void setReminderDate(Date reminderDate);

    @NotNull
    String getReminderType();  // "DEADLINE_SOON", "OVERDUE", "CUSTOM"
    void setReminderType(String reminderType);

    boolean isSent();
    void setSent(boolean sent);

    Date getSentDate();
    void setSentDate(Date sentDate);

    @StringLength(StringLength.UNLIMITED)
    String getMessage();
    void setMessage(String message);
}
