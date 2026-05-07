package com.projmodel.plugin.report;

import com.projmodel.plugin.dto.IssueViewDTO;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Утилитный класс для генерации отчётов в форматах HTML (открывается в Word) и TXT
 * Не требует внешних библиотек — только стандартная Java
 */
public class ReportGenerator {

    /**
     * Сгенерировать HTML-отчёт по списку задач.
     * Можно открыть в браузере или в Microsoft Word (Файл → Открыть).
     */
    public static byte[] generateHtmlReport(List<IssueViewDTO> issues, String projectKey) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ru\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"windows-1251\">\n");
        html.append("<title>Отчёт по проекту ").append(escapeHtml(projectKey)).append("</title>\n");
        html.append("<style>\n");
        html.append("  body { font-family: Arial, sans-serif; margin: 40px; color: #333; }\n");
        html.append("  h1 { color: #0052cc; border-bottom: 2px solid #0052cc; padding-bottom: 10px; }\n");
        html.append("  .date { color: #666; font-size: 14px; margin-bottom: 30px; }\n");
        html.append("  table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        html.append("  th { background-color: #0052cc; color: white; padding: 12px; text-align: left; font-size: 14px; }\n");
        html.append("  td { padding: 10px; border-bottom: 1px solid #ddd; font-size: 13px; }\n");
        html.append("  tr:hover { background-color: #f5f5f5; }\n");
        html.append("  .no-due { color: #999; font-style: italic; }\n");
        html.append("  .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #999; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Заголовок
        html.append("<h1>Отчёт по проекту ").append(escapeHtml(projectKey)).append("</h1>\n");

        // Дата создания
        String dateStr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
        html.append("<p class=\"date\">Создан: ").append(dateStr).append("</p>\n");

        // Таблица с задачами
        html.append("<table>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("  <th>Ключ</th>\n");
        html.append("  <th>Название</th>\n");
        html.append("  <th>Статус</th>\n");
        html.append("  <th>Исполнитель</th>\n");
        html.append("  <th>Дедлайн</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (IssueViewDTO issue : issues) {
            html.append("<tr>\n");
            html.append("  <td><strong>").append(escapeHtml(issue.get_key())).append("</strong></td>\n");
            html.append("  <td>").append(escapeHtml(issue.get_summary())).append("</td>\n");
            html.append("  <td>").append(escapeHtml(issue.get_status())).append("</td>\n");
            html.append("  <td>").append(escapeHtml(issue.getAssignee())).append("</td>\n");

            if (issue.getDueDate() != null) {
                String dueStr = new SimpleDateFormat("dd.MM.yyyy").format(issue.getDueDate());
                html.append("  <td>").append(dueStr).append("</td>\n");
            } else {
                html.append("  <td class=\"no-due\">Нет</td>\n");
            }

            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // Футер
        html.append("<div class=\"footer\">\n");
        html.append("  <p>Отчёт создан автоматически плагином ProjModel для Jira</p>\n");
        html.append("  <p>Всего задач: ").append(issues.size()).append("</p>\n");
        html.append("</div>\n");

        html.append("</body>\n");
        html.append("</html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Сгенерировать простой текстовый отчёт.
     */
    public static byte[] generateTextReport(List<IssueViewDTO> issues, String projectKey) {
        StringBuilder text = new StringBuilder();

        text.append("========================================\n");
        text.append("  ОТЧЁТ ПО ПРОЕКТУ ").append(projectKey).append("\n");
        text.append("========================================\n\n");

        String dateStr = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
        text.append("Создан: ").append(dateStr).append("\n\n");

        text.append(String.format("%-15s %-40s %-15s %-20s %-12s\n",
                "Ключ", "Название", "Статус", "Исполнитель", "Дедлайн"));
        text.append(String.format("%-15s %-40s %-15s %-20s %-12s\n",
                "-----", "--------", "------", "------------", "--------"));

        for (IssueViewDTO issue : issues) {
            String dueStr = issue.getDueDate() != null
                    ? new SimpleDateFormat("dd.MM.yyyy").format(issue.getDueDate())
                    : "Нет";

            text.append(String.format("%-15s %-40s %-15s %-20s %-12s\n",
                    truncate(issue.get_key(), 15),
                    truncate(issue.get_summary(), 40),
                    truncate(issue.get_status(), 15),
                    truncate(issue.getAssignee(), 20),
                    dueStr));
        }

        text.append("\n----------------------------------------\n");
        text.append("Всего задач: ").append(issues.size()).append("\n");
        text.append("Отчёт создан плагином ProjModel для Jira\n");

        return text.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}