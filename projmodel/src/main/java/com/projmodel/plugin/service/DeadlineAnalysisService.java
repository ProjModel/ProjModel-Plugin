package com.projmodel.plugin.service;

import com.projmodel.plugin.dto.DeadlineIssueDTO;

import java.util.List;

public interface DeadlineAnalysisService {
    List<DeadlineIssueDTO> analyzeDeadlines(String projectKey);
}
