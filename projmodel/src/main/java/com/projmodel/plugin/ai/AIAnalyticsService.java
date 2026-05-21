package com.projmodel.plugin.ai;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.gson.Gson;
import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;

import javax.inject.Named;
import java.io.IOException;
import java.util.List;

@Named
@ExportAsService
public class AIAnalyticsService {
    private final AIClient aiClient;
    private final Gson gson;

    public AIAnalyticsService() {
        this.aiClient = new AIClient();
        this.gson = new Gson();
    }

    public AIAnalyticsService(AIClient aiClient) {
        this.aiClient = aiClient;
        this.gson = new Gson();
    }

    public String analyzeDeadlines(List<DeadlineIssueDTO> deadlineIssues) throws IOException {
        String systemPrompt = "Ты - AI-аналитик для Jira плагина ProjModel. " +
                "Твоя задача - анализировать дедлайны задач и давать рекомендации. " +
                "Отвечай на русском языке. Форматируй ответ красиво с использованием HTML-тегов: " +
                "<h3> для подзаголовков, <ul><li> для списков, <strong> для выделения важного.";

        String userMessage = "Проанализируй следующие задачи проекта и их дедлайны. " +
                "Выяви проблемные зоны, риски срыва сроков и дай конкретные рекомендации по управлению дедлайнами:\n\n" +
                gson.toJson(deadlineIssues);

        return aiClient.sendMessage(systemPrompt, userMessage);
    }

    public String analyzeWorkload(List<WorkloadViewDTO> workloadData) throws IOException {
        String systemPrompt = "Ты - AI-аналитик для Jira плагина ProjModel. " +
                "Твоя задача - анализировать загрузку участников команды и давать рекомендации " +
                "по перераспределению задач. " +
                "Отвечай на русском языке. Форматируй ответ красиво с использованием HTML-тегов: " +
                "<h3> для подзаголовков, <ul><li> для списков, <strong> для выделения важного.";

        String userMessage = "Проанализируй загрузку участников команды. " +
                "Выяви перегруженных и недогруженных сотрудников. " +
                "Предложи оптимальное перераспределение задач:\n\n" +
                gson.toJson(workloadData);

        return aiClient.sendMessage(systemPrompt, userMessage);
    }

    public String getGeneralRecommendations(List<DeadlineIssueDTO> deadlineIssues,
                                            List<WorkloadViewDTO> workloadData) throws IOException {
        String systemPrompt = "Ты - AI-аналитик для Jira плагина ProjModel. " +
                "Твоя задача - дать комплексный анализ проекта на основе данных о дедлайнах и загрузке команды. " +
                "Отвечай на русском языке. Форматируй ответ красиво с использованием HTML-тегов: " +
                "<h3> для подзаголовков, <ul><li> для списков, <strong> для выделения важного.";

        StringBuilder userMessage = new StringBuilder();
        userMessage.append("Дай комплексный анализ проекта на основе следующих данных:\n\n");
        userMessage.append("=== ДЕДЛАЙНЫ ===\n");
        userMessage.append(gson.toJson(deadlineIssues));
        userMessage.append("\n\n=== ЗАГРУЗКА КОМАНДЫ ===\n");
        userMessage.append(gson.toJson(workloadData));

        return aiClient.sendMessage(systemPrompt, userMessage.toString());
    }
}