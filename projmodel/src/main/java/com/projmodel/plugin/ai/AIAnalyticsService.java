package com.projmodel.plugin.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projmodel.plugin.dto.DeadlineIssueDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        Date now = new Date();
        StringBuilder data = new StringBuilder();

        data.append("Текущая дата: ").append(sdf.format(now)).append("\n\n");

        for (DeadlineIssueDTO issue : deadlineIssues) {
            data.append("• ").append(issue.getIssueKey())
                    .append(" - \"").append(issue.getSummary()).append("\"\n");
            data.append("  Статус: ").append(issue.getStatus())
                    .append(" | Исполнитель: ").append(issue.getAssignee()).append("\n");
            if (issue.getDueDate() != null) {
                long daysLeft = (issue.getDueDate().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                data.append("  Дедлайн: ").append(sdf.format(issue.getDueDate()))
                        .append(" (осталось ").append(daysLeft).append(" дней)\n");
            } else {
                data.append("  Дедлайн: НЕ УСТАНОВЛЕН\n");
            }
            data.append("  Системный риск: ").append(issue.getRiskLevel()).append("\n\n");
        }

        String prompt = "Ты — ОПЫТНЫЙ Project Manager и AI-аналитик. Твоя задача — ГЛУБОКО проанализировать дедлайны задач.\n\n" +
                "ВАЖНЫЕ ПРАВИЛА АНАЛИЗА:\n" +
                "1. ВНИМАТЕЛЬНО читай названия задач. Оценивай их РЕАЛЬНУЮ сложность:\n" +
                "   - \"Сделать копию GTA 5\" — это НЕВЫПОЛНИМАЯ задача для одного человека за любое время\n" +
                "   - \"Написать 2 цифры на листочке\" — это задача на 1 минуту, дедлайн в год — АБСУРД\n" +
                "   - \"Создать логотип\" — реально за 3-5 дней\n" +
                "   - \"Написать API\" — зависит от сложности, минимум 1-2 недели\n\n" +
                "2. Оценивай КАЖДУЮ задачу в Story Points (1 SP = ~1 день работы):\n" +
                "   - 1 SP: простые задачи (написать текст, поправить баг)\n" +
                "   - 3-5 SP: средние (создать компонент, написать тесты)\n" +
                "   - 8-13 SP: сложные (разработать модуль, интеграция)\n" +
                "   - 21+ SP: очень сложные/нереалистичные\n\n" +
                "3. СРАВНИВАЙ свою оценку в SP с оставшимся временем:\n" +
                "   - Если задача на 13 SP, а осталось 3 дня — это КРИТИЧЕСКИЙ РИСК\n" +
                "   - Если задача на 1 SP, а остался месяц — риска нет\n\n" +
                "4. НЕ ДОВЕРЯЙ системной оценке риска слепо. Система считает только дни до дедлайна.\n" +
                "   Ты должен оценить РЕАЛЬНЫЙ риск с учётом сложности задачи.\n\n" +
                "ФОРМАТ ОТВЕТА:\n" +
                "Разбей ответ на разделы с заголовками. Для каждой задачи дай:\n" +
                "- Свою оценку в Story Points и почему\n" +
                "- Реальный риск (критический/высокий/средний/низкий) с объяснением\n" +
                "- Конкретную рекомендацию: что делать с этой задачей\n\n" +
                "Пиши на русском. Будь честным — если задача нереалистична, скажи об этом прямо.\n\n" +
                "ДАННЫЕ:\n" + data.toString();

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
            data.append("  Системная оценка нагрузки: ").append(loadEmoji).append("\n\n");
        }

        String prompt = "Ты — ОПЫТНЫЙ Team Lead. Проанализируй загрузку команды ГЛУБОКО.\n\n" +
                "ПРАВИЛА АНАЛИЗА:\n" +
                "1. Системная оценка нагрузки ОЧЕНЬ ПРОСТАЯ — она считает только количество задач.\n" +
                "   Ты должен учесть, что:\n" +
                "   - 1 сложная задача > 5 простых\n" +
                "   - 3 просроченные задачи = катастрофа, даже если остальные в порядке\n" +
                "   - Задачи без дедлайна = риск, что они никогда не будут сделаны\n\n" +
                "2. Дай ОСМЫСЛЕННУЮ оценку нагрузки для каждого сотрудника:\n" +
                "   - Низкая нагрузка: можно брать ещё задачи\n" +
                "   - Средняя: оптимально, но следить\n" +
                "   - Высокая: нужна помощь или пересмотр приоритетов\n" +
                "   - Критическая: срочно разгружать, иначе burnout\n\n" +
                "3. Предложи КОНКРЕТНЫЙ план перераспределения:\n" +
                "   - Кому передать задачи\n" +
                "   - Какие задачи можно отложить\n" +
                "   - Кому нужна помощь\n\n" +
                "ФОРМАТ: Разбей на разделы. Пиши конкретно, с именами и действиями.\n\n" +
                "ДАННЫЕ:\n" + data.toString();

        return aiClient.sendMessage(prompt, "");
    }

    public String getGeneralRecommendations(List<DeadlineIssueDTO> deadlineIssues,
                                            List<WorkloadViewDTO> workloadData) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date now = new Date();
        StringBuilder data = new StringBuilder();

        data.append("Текущая дата: ").append(sdf.format(now)).append("\n\n");
        data.append("=== ЗАДАЧИ И ДЕДЛАЙНЫ ===\n");
        for (DeadlineIssueDTO issue : deadlineIssues) {
            data.append("• ").append(issue.getIssueKey())
                    .append(" - \"").append(issue.getSummary()).append("\"\n");
            data.append("  Статус: ").append(issue.getStatus())
                    .append(" | Исполнитель: ").append(issue.getAssignee()).append("\n");
            if (issue.getDueDate() != null) {
                long daysLeft = (issue.getDueDate().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                data.append("  Дедлайн: ").append(sdf.format(issue.getDueDate()))
                        .append(" (через ").append(daysLeft).append(" дней)\n");
            } else {
                data.append("  Дедлайн: НЕТ\n");
            }
            data.append("  Системный риск: ").append(issue.getRiskLevel()).append("\n\n");
        }

        data.append("=== ЗАГРУЗКА КОМАНДЫ ===\n");
        for (WorkloadViewDTO w : workloadData) {
            data.append("• ").append(w.getAssignee())
                    .append(": ").append(w.getTotalTasks()).append(" задач")
                    .append(" | Просрочено: ").append(w.getOverdueTasks())
                    .append(" | Срочных: ").append(w.getTasksDueWithin7Days())
                    .append(" | Без дедлайна: ").append(w.getTasksWithoutDueDate())
                    .append(" | Нагрузка: ").append(w.getLoadLevel()).append("\n");
        }

        String prompt = "Ты — ОПЫТНЫЙ Project Manager и AI-консультант. Дай КОМПЛЕКСНЫЙ и ЧЕСТНЫЙ анализ проекта.\n\n" +
                "КЛЮЧЕВЫЕ ПРИНЦИПЫ:\n" +
                "1. АНАЛИЗИРУЙ НАЗВАНИЯ ЗАДАЧ ГЛУБОКО:\n" +
                "   - \"Сделать копию GTA 5\" за 8 дней — это АБСОЛЮТНО НЕРЕАЛИСТИЧНО\n" +
                "   - \"Написать 2 цифры на листочке\" за год — это НЕЛЕПО\n" +
                "   - Оценивай каждую задачу в Story Points (1 SP ≈ 1 день работы)\n\n" +
                "2. НЕ ДОВЕРЯЙ СИСТЕМНЫМ ОЦЕНКАМ РИСКА:\n" +
                "   - Система считает только дни до дедлайна\n" +
                "   - Ты должен учесть СЛОЖНОСТЬ задачи\n" +
                "   - Задача может иметь риск LOW по срокам, но быть НЕВЫПОЛНИМОЙ по сути\n\n" +
                "3. Дай ЧЕСТНУЮ ОЦЕНКУ:\n" +
                "   - Если задача нереалистична — СКАЖИ ОБ ЭТОМ ПРЯМО\n" +
                "   - Если дедлайн абсурден — ОТМЕТЬ ЭТО\n" +
                "   - Предложи РЕАЛИСТИЧНЫЕ сроки и решения\n\n" +
                "СТРУКТУРА ОТВЕТА:\n" +
                "1. ОБЩАЯ ОЦЕНКА — одной фразой: проект в порядке / есть проблемы / кризис\n" +
                "2. АНАЛИЗ КАЖДОЙ ЗАДАЧИ — с оценкой в SP, реальным риском и рекомендациями\n" +
                "3. АНАЛИЗ КОМАНДЫ — кто справится, кто нет\n" +
                "4. ГЛАВНЫЕ РИСКИ — 2-3 самых опасных\n" +
                "5. ПЛАН ДЕЙСТВИЙ — 5-6 конкретных шагов\n" +
                "6. ПРОГНОЗ — реалистичный прогноз по проекту\n\n" +
                "Пиши на русском. Будь ПРЯМЫМ и КОНКРЕТНЫМ. Не используй общие фразы.\n\n" +
                "ДАННЫЕ:\n" + data.toString();

        return aiClient.sendMessage(prompt, "");
    }
}