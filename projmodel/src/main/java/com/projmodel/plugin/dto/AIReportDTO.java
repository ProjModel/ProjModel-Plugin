package com.projmodel.plugin.dto;

import java.util.Map;

public class AIReportDTO {
    private String analysis;
    private Map<String, String> recommendations;
    private String riskLevel;

    public AIReportDTO() {}

    public AIReportDTO(String analysis, Map<String, String> recommendations, String riskLevel) {
        this.analysis = analysis;
        this.recommendations = recommendations;
        this.riskLevel = riskLevel;
    }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public Map<String, String> getRecommendations() { return recommendations; }
    public void setRecommendations(Map<String, String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}