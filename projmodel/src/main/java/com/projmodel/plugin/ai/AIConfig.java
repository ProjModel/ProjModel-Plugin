package com.projmodel.plugin.ai;

public class AIConfig {
    private String apiKey;

    public AIConfig() {

        this.apiKey = "";
    }

    public AIConfig(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}