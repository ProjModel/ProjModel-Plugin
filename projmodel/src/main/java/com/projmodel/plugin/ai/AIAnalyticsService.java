package com.projmodel.plugin.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AIAnalyticsService {
    private final AIClient aiClient;
    private final Gson gson;

    public AIAnalyticsService() {
        this.aiClient = new AIClient();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public AIAnalyticsService(AIClient aiClient) {
        this.aiClient = aiClient;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String analyzeDeadlines(List<DeadlineIssueDTO> deadlineIssues) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        StringBuilder data = new StringBuilder();

        for (DeadlineIssueDTO issue : deadlineIssues) {
            data.append("• ").append(issue.getIssueKey())
                    .append(" - ").append(issue.getSummary()).append("\n");
            data.append("  Статус: ").append(issue.getStatus())
                    .append(" | Исполнитель: ").append(issue.getAssignee()).append("\n");
            if (issue.getDueDate() != null) {
                data.append("  Дедлайн: ").append(sdf.format(issue.getDueDate())).append("\n");
            } else {
                data.append("  Дедлайн: НЕ УСТАНОВЛЕН\n");
            }
            data.append("  Уровень риска: ").append(issue.getRiskLevel()).append("\n\n");
        }

        String prompt = "Ты — AI-аналитик плагина ProjModel для Jira. Проанализируй дедлайны задач и дай рекомендации.Используй Story Points для анализа.\n\n" +
                "В ответе должны быть:\n" +
                "- Общая оценка ситуации с дедлайнами\n" +
                "- Перечень критических и проблемных задач\n" +
                "- Конкретные рекомендации по управлению сроками\n\n" +
                "Пиши на русском языке. Будь конкретным и полезным. Обращай внимание на название задач.\n\n" +
                "ДАННЫЕ ДЛЯ АНАЛИЗА:\n" + data.toString();

        return aiClient.sendMessage(prompt, "");
    }

    public String analyzeWorkload(List<WorkloadViewDTO> workloadData) throws IOException {
        StringBuilder data = new StringBuilder();

        for (WorkloadViewDTO w : workloadData) {
            String loadEmoji;
            switch (w.getLoadLevel()) {
                case "critical": loadEmoji = "🔴 КРИТИЧЕСКАЯ"; break;
                case "high": loadEmoji = "🟠 ВЫСОКАЯ"; break;
                case "medium": loadEmoji = "🟡 СРЕДНЯЯ"; break;
                default: loadEmoji = "🟢 НИЗКАЯ"; break;
            }

            data.append("• ").append(w.getAssignee()).append("\n");
            data.append("  Всего задач: ").append(w.getTotalTasks())
                    .append(" | Просрочено: ").append(w.getOverdueTasks())
                    .append(" | Срочных (≤7 дней): ").append(w.getTasksDueWithin7Days())
                    .append(" | Без дедлайна: ").append(w.getTasksWithoutDueDate()).append("\n");
            data.append("  Нагрузка: ").append(loadEmoji).append("\n\n");
        }

        String prompt = "Ты — AI-аналитик плагина ProjModel для Jira. Проанализируй загрузку команды и дай рекомендации.\n\n" +
                "В ответе должны быть:\n" +
                "- Общая оценка загрузки команды\n" +
                "- Кто перегружен и кто недогружен\n" +
                "- Рекомендации по перераспределению задач\n\n" +
                "Пиши на русском языке. Будь конкретным.Обращай внимание на название задач.\n\n" +
                "ДАННЫЕ ДЛЯ АНАЛИЗА:\n" + data.toString();

        return aiClient.sendMessage(prompt, "");
    }

    public String getGeneralRecommendations(List<DeadlineIssueDTO> deadlineIssues,
                                            List<WorkloadViewDTO> workloadData) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        StringBuilder data = new StringBuilder();

        data.append("=== ДЕДЛАЙНЫ ===\n");
        for (DeadlineIssueDTO issue : deadlineIssues) {
            data.append("• ").append(issue.getIssueKey())
                    .append(" - ").append(issue.getSummary())
                    .append(" | Статус: ").append(issue.getStatus())
                    .append(" | Исполнитель: ").append(issue.getAssignee());
            if (issue.getDueDate() != null) {
                data.append(" | Дедлайн: ").append(sdf.format(issue.getDueDate()));
            }
            data.append(" | Риск: ").append(issue.getRiskLevel()).append("\n");
        }

        data.append("\n=== ЗАГРУЗКА КОМАНДЫ ===\n");
        for (WorkloadViewDTO w : workloadData) {
            data.append("• ").append(w.getAssignee())
                    .append(" | Задач: ").append(w.getTotalTasks())
                    .append(" | Просрочено: ").append(w.getOverdueTasks())
                    .append(" | Срочных: ").append(w.getTasksDueWithin7Days())
                    .append(" | Нагрузка: ").append(w.getLoadLevel()).append("\n");
        }

        String prompt = "Ты — AI-аналитик плагина ProjModel для Jira. Дай комплексный анализ проекта.\n\n" +
                "В ответе должны быть:\n" +
                "- Общее состояние проекта (хорошо/проблемы/критично)\n" +
                "- Основные риски (2-3 самых важных)\n" +
                "- Рекомендации (5-6 конкретных шагов)\n" +
                "- Прогноз по срокам\n\n" +
                "- Обращай внимание на название задач и анализируй насколько они реальная для выполнения за данный промежуток\n\n" +
                "Пиши на русском языке. Будь конкретным.\n\n" +
                "ДАННЫЕ ДЛЯ АНАЛИЗА:\n" + data.toString();

        return aiClient.sendMessage(prompt, "");
    }
}