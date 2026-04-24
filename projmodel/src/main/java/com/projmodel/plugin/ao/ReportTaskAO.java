package com.projmodel.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import java.util.Date;

@Preload
public interface ReportTaskAO extends Entity {

    @NotNull
    String getProjectKey();
    void setProjectKey(String projectKey);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getIssueKeys();  // JSON-массив ключей задач
    void setIssueKeys(String issueKeys);

    String getReportFormat();  // "WORD" или "PDF"
    void setReportFormat(String reportFormat);

    Date getCreatedDate();
    void setCreatedDate(Date createdDate);

    String getStatus();  // "PENDING", "GENERATED", "ERROR"
    void setStatus(String status);

    @StringLength(StringLength.UNLIMITED)
    String getFilePath();
    void setFilePath(String filePath);
}
