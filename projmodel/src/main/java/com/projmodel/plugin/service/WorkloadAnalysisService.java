package com.projmodel.plugin.service;

import com.projmodel.plugin.dto.WorkloadViewDTO;
import java.util.List;

public interface WorkloadAnalysisService {
    List<WorkloadViewDTO> analyzeWorkload(String projectKey);
}