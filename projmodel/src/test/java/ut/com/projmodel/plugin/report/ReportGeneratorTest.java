package ut.com.projmodel.plugin.report;

import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.report.ReportGenerator;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Модульные тесты для ReportGenerator
 */
public class ReportGeneratorTest {

    private List<IssueViewDTO> testIssues;
    private Date testDueDate;

    @Before
    public void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.MARCH, 15); // 15 марта 2026
        testDueDate = calendar.getTime();

        testIssues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Первая задача", "Open", "Иван Петров", testDueDate),
                new IssueViewDTO("TEST-2", "Вторая задача", "In Progress", "Мария Иванова", null),
                new IssueViewDTO("TEST-3", "Third <script>alert('xss')</script>", "Done", "John & Doe", testDueDate)
        );
    }

    // ==================== Тесты для generateHtmlReport ====================

    @Test
    public void testGenerateHtmlReport_WithValidIssues() {
        // Act
        byte[] result = ReportGenerator.generateHtmlReport(testIssues, "TEST");
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert - проверяем структуру HTML
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Результат должен содержать данные", result.length > 0);

        // Проверяем основные элементы HTML
        assertTrue("Должен быть DOCTYPE", html.contains("<!DOCTYPE html>"));
        assertTrue("Должен быть заголовок проекта", html.contains("Отчёт по проекту TEST"));
        assertTrue("Должен быть тег таблицы", html.contains("<table>"));
        assertTrue("Должен быть тег tbody", html.contains("<tbody>"));

        // Проверяем наличие ключей задач
        assertTrue("Должен содержать TEST-1", html.contains("<strong>TEST-1</strong>"));
        assertTrue("Должен содержать TEST-2", html.contains("<strong>TEST-2</strong>"));

        // Проверяем футер
        assertTrue("Должен быть счетчик задач", html.contains("Всего задач: 3"));

        // Проверяем дату создания
        String todayStr = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        assertTrue("Должна быть сегодняшняя дата", html.contains(todayStr));
    }

    @Test
    public void testGenerateHtmlReport_WithDueDate() {
        // Act
        byte[] result = ReportGenerator.generateHtmlReport(testIssues, "TEST");
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert
        String dueStr = new SimpleDateFormat("dd.MM.yyyy").format(testDueDate);
        assertTrue("Должна быть дата дедлайна", html.contains(dueStr));
    }

    @Test
    public void testGenerateHtmlReport_WithNullDueDate() {
        // Act
        byte[] result = ReportGenerator.generateHtmlReport(testIssues, "TEST");
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert - для TEST-2 установлен null дедлайн
        assertTrue("Должен быть указан отсутствующий дедлайн", html.contains("Нет"));
        assertTrue("Должен быть класс no-due", html.contains("class=\"no-due\""));
    }

    @Test
    public void testGenerateHtmlReport_EmptyList() {
        // Arrange
        List<IssueViewDTO> emptyList = Collections.emptyList();

        // Act
        byte[] result = ReportGenerator.generateHtmlReport(emptyList, "EMPTY");
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull("Результат с пустым списком не должен быть null", result);
        assertTrue("Должен содержать заголовок", html.contains("Отчёт по проекту EMPTY"));
        assertTrue("Должен быть счетчик 0", html.contains("Всего задач: 0"));
        assertFalse("Не должно быть строк таблицы", html.contains("<td><strong>"));
    }

    // ==================== Тесты для generateTextReport ====================

    @Test
    public void testGenerateTextReport_WithValidIssues() {
        // Act
        byte[] result = ReportGenerator.generateTextReport(testIssues, "TEST");
        String text = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Результат должен содержать данные", result.length > 0);

        assertTrue("Должен быть заголовок", text.contains("ОТЧЁТ ПО ПРОЕКТУ TEST"));
        assertTrue("Должны быть заголовки колонок", text.contains("Ключ"));
        assertTrue("Должны быть заголовки колонок", text.contains("Название"));
        assertTrue("Должны быть заголовки колонок", text.contains("Статус"));
        assertTrue("Должны быть заголовки колонок", text.contains("Исполнитель"));
        assertTrue("Должны быть заголовки колонок", text.contains("Дедлайн"));

        assertTrue("Должен содержать TEST-1", text.contains("TEST-1"));
        assertTrue("Должен содержать TEST-2", text.contains("TEST-2"));
        assertTrue("Должен содержать TEST-3", text.contains("TEST-3"));

        assertTrue("Должен быть счетчик задач", text.contains("Всего задач: 3"));
    }

    @Test
    public void testGenerateTextReport_EmptyList() {
        // Arrange
        List<IssueViewDTO> emptyList = Collections.emptyList();

        // Act
        byte[] result = ReportGenerator.generateTextReport(emptyList, "EMPTY");
        String text = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull("Результат с пустым списком не должен быть null", result);
        assertTrue("Должен быть счетчик 0", text.contains("Всего задач: 0"));
    }

    // ==================== Тесты для escapeHtml (через generateHtmlReport) ====================

    @Test
    public void testGenerateHtmlReport_XssProtection() {
        // Act
        byte[] result = ReportGenerator.generateHtmlReport(testIssues, "TEST");
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert - проверяем экранирование опасных символов
        assertFalse("Не должно быть тега script", html.contains("<script>"));
        assertTrue("Должен быть экранированный script", html.contains("&lt;script&gt;"));
        assertTrue("Должен быть экранированный &", html.contains("&amp;"));
    }

    @Test
    public void testGenerateHtmlReport_SpecialCharactersInProjectKey() {
        // Arrange
        String projectKeyWithTags = "TEST<script>";

        // Act
        byte[] result = ReportGenerator.generateHtmlReport(testIssues, projectKeyWithTags);
        String html = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertFalse("Ключ проекта не должен содержать script", html.contains("<script>test</script>"));
    }

    // ==================== Тесты для truncate (через generateTextReport) ====================

    @Test
    public void testGenerateTextReport_LongTextTruncated() {
        // Arrange
        String longSummary = "Очень длинное название задачи которое точно превышает сорок символов и должно обрезаться";
        IssueViewDTO longIssue = new IssueViewDTO("LONG-1", longSummary, "Open", "Assignee", null);
        List<IssueViewDTO> issues = Collections.singletonList(longIssue);

        // Act
        byte[] result = ReportGenerator.generateTextReport(issues, "TEST");
        String text = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertTrue("Длинный текст должен быть обрезан с ...", text.contains("..."));
        assertFalse("Полный длинный текст не должен присутствовать", text.contains(longSummary));
    }

    @Test
    public void testGenerateTextReport_NullValues() {
        // Arrange
        IssueViewDTO nullIssue = new IssueViewDTO(null, null, null, null, null);
        List<IssueViewDTO> issues = Collections.singletonList(nullIssue);

        // Act
        byte[] result = ReportGenerator.generateTextReport(issues, "TEST");
        String text = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull("Результат с null значениями не должен быть null", result);
        // Не должно быть исключений, null значения должны обрабатываться
    }

    // ==================== Граничные тесты ====================

    @Test
    public void testGenerateHtmlReport_VeryLargeDataSet() {
        // Arrange
        List<IssueViewDTO> largeIssues = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeIssues.add(new IssueViewDTO(
                    "TEST-" + i,
                    "Задача номер " + i,
                    "Open",
                    "User" + (i % 10),
                    new Date()
            ));
        }

        // Act
        byte[] result = ReportGenerator.generateHtmlReport(largeIssues, "LARGE");

        // Assert
        assertNotNull("Результат с большими данными не должен быть null", result);
        assertTrue("Результат должен быть больше 10KB", result.length > 10000);
    }

    @Test
    public void testGenerateReports_BothFormatsConsistent() {
        // Act
        byte[] htmlResult = ReportGenerator.generateHtmlReport(testIssues, "TEST");
        byte[] textResult = ReportGenerator.generateTextReport(testIssues, "TEST");

        String html = new String(htmlResult, StandardCharsets.UTF_8);
        String text = new String(textResult, StandardCharsets.UTF_8);

        // Assert - оба формата должны содержать одинаковые ключевые данные
        assertTrue("HTML должен содержать TEST-1", html.contains("TEST-1"));
        assertTrue("Текст должен содержать TEST-1", text.contains("TEST-1"));

        assertTrue("HTML должен содержать счетчик 3", html.contains("Всего задач: 3"));
        assertTrue("Текст должен содержать счетчик 3", text.contains("Всего задач: 3"));
    }

    @Test
    public void testGenerateTextReport_WithDueDateAndWithout() {
        // Act
        byte[] result = ReportGenerator.generateTextReport(testIssues, "TEST");
        String text = new String(result, StandardCharsets.UTF_8);

        // Assert
        String dueStr = new SimpleDateFormat("dd.MM.yyyy").format(testDueDate);
        assertTrue("Текст должен содержать дату дедлайна для TEST-1", text.contains(dueStr));
        assertTrue("Текст должен содержать 'Нет' для TEST-2 без дедлайна", text.contains("Нет"));
    }
}