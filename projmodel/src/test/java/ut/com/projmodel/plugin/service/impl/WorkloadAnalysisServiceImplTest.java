package ut.com.projmodel.plugin.service.impl;

import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.dto.WorkloadViewDTO;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.impl.WorkloadAnalysisServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Модульные тесты для WorkloadAnalysisServiceImpl (без Mockito)
 */
public class WorkloadAnalysisServiceImplTest {

    private WorkloadAnalysisServiceImpl workloadAnalysisService;
    private FakeIssueDataService fakeIssueDataService;

    private Date yesterday;
    private Date tomorrow;
    private Date in3Days;
    private Date in10Days;
    private Date nextMonth;

    @Before
    public void setUp() {
        fakeIssueDataService = new FakeIssueDataService();
        workloadAnalysisService = new WorkloadAnalysisServiceImpl(fakeIssueDataService);

        Calendar calendar = Calendar.getInstance();

        // Вчера (просроченный дедлайн)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        yesterday = calendar.getTime();

        // Завтра (в пределах 7 дней)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow = calendar.getTime();

        // Через 3 дня (в пределах 7 дней)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        in3Days = calendar.getTime();

        // Через 10 дней (за пределами 7 дней)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        in10Days = calendar.getTime();

        // Через месяц
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        nextMonth = calendar.getTime();
    }

    // ==================== Тесты для analyzeWorkload ====================

    @Test
    public void testAnalyzeWorkload_EmptyProject() {
        // Arrange
        String projectKey = "EMPTY";
        fakeIssueDataService.setOpenIssues(projectKey, Collections.emptyList());

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Список должен быть пустым", result.isEmpty());
    }

    @Test
    public void testAnalyzeWorkload_NullIssueList() {
        // Arrange
        String projectKey = "TEST";
        fakeIssueDataService.setOpenIssues(projectKey, null);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Список должен быть пустым для null", result.isEmpty());
    }

    @Test
    public void testAnalyzeWorkload_SingleAssigneeLowLoad() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Задача 1", "Open", "Иван Петров", nextMonth),
                new IssueViewDTO("TEST-2", "Задача 2", "In Progress", "Иван Петров", in10Days)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertNotNull("Результат не должен быть null", result);
        assertEquals("Должен быть 1 исполнитель", 1, result.size());

        WorkloadViewDTO workload = result.get(0);
        assertEquals("Иван Петров", workload.getAssignee());
        assertEquals("Общее количество задач", 2, workload.getTotalTasks());
        assertEquals("Задач с дедлайном в 7 дней", 0, workload.getTasksDueWithin7Days());
        assertEquals("Просроченных задач", 0, workload.getOverdueTasks());
        assertEquals("Задач без дедлайна", 0, workload.getTasksWithoutDueDate());
        assertEquals("Уровень нагрузки", "low", workload.getLoadLevel());
        assertEquals("Процент нагрузки", 40, workload.getLoadPercent());
    }

    @Test
    public void testAnalyzeWorkload_MediumLoad() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = new ArrayList<>();

        // 5 задач для одного исполнителя (больше 4 -> medium)
        for (int i = 1; i <= 5; i++) {
            issues.add(new IssueViewDTO("TEST-" + i, "Задача " + i, "Open", "Иван Петров", nextMonth));
        }

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(5, workload.getTotalTasks());
        assertEquals("medium", workload.getLoadLevel());
        assertEquals(70, workload.getLoadPercent());
    }

    @Test
    public void testAnalyzeWorkload_HighLoad_WithOverdue() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Просроченная", "Open", "Мария Иванова", yesterday),
                new IssueViewDTO("TEST-2", "Срочная", "Open", "Мария Иванова", tomorrow),
                new IssueViewDTO("TEST-3", "Обычная", "Open", "Мария Иванова", nextMonth)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(3, workload.getTotalTasks());
        assertEquals(1, workload.getTasksDueWithin7Days());
        assertEquals(1, workload.getOverdueTasks());
        assertEquals("high", workload.getLoadLevel());
        assertEquals(90, workload.getLoadPercent());
    }

    @Test
    public void testAnalyzeWorkload_CriticalLoad_ManyOverdue() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Просрочено 1", "Open", "John Doe", yesterday),
                new IssueViewDTO("TEST-2", "Просрочено 2", "Open", "John Doe", yesterday),
                new IssueViewDTO("TEST-3", "Просрочено 3", "Open", "John Doe", yesterday)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(3, workload.getOverdueTasks());
        assertEquals("critical", workload.getLoadLevel());
        assertEquals(110, workload.getLoadPercent());
    }

    @Test
    public void testAnalyzeWorkload_CriticalLoad_ManyTasks() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = new ArrayList<>();

        // 11 задач (больше 10 -> critical)
        for (int i = 1; i <= 11; i++) {
            issues.add(new IssueViewDTO("TEST-" + i, "Задача " + i, "Open", "John Doe", nextMonth));
        }

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(11, workload.getTotalTasks());
        assertEquals("critical", workload.getLoadLevel());
    }

    @Test
    public void testAnalyzeWorkload_HighLoad_ManyUrgentTasks() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Срочно 1", "Open", "John Doe", tomorrow),
                new IssueViewDTO("TEST-2", "Срочно 2", "Open", "John Doe", in3Days),
                new IssueViewDTO("TEST-3", "Срочно 3", "Open", "John Doe", in3Days),
                new IssueViewDTO("TEST-4", "Срочно 4", "Open", "John Doe", in3Days)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(4, workload.getTasksDueWithin7Days());
        assertEquals("high", workload.getLoadLevel());
    }

    @Test
    public void testAnalyzeWorkload_TasksWithoutDueDate() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Без дедлайна 1", "Open", "Иван Петров", null),
                new IssueViewDTO("TEST-2", "Без дедлайна 2", "Open", "Иван Петров", null)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals(2, workload.getTasksWithoutDueDate());
        assertEquals(0, workload.getTasksDueWithin7Days());
        assertEquals(0, workload.getOverdueTasks());
    }

    @Test
    public void testAnalyzeWorkload_MultipleAssignees() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                // Иван - низкая нагрузка
                new IssueViewDTO("TEST-1", "Задача Ивана 1", "Open", "Иван Петров", nextMonth),
                new IssueViewDTO("TEST-2", "Задача Ивана 2", "Open", "Иван Петров", in10Days),

                // Мария - критическая нагрузка
                new IssueViewDTO("TEST-3", "Просрочено", "Open", "Мария Иванова", yesterday),
                new IssueViewDTO("TEST-4", "Просрочено", "Open", "Мария Иванова", yesterday),
                new IssueViewDTO("TEST-5", "Просрочено", "Open", "Мария Иванова", yesterday),

                // John - высокая нагрузка (8 задач, >7)
                new IssueViewDTO("TEST-6", "Задача John 1", "Open", "John Doe", nextMonth),
                new IssueViewDTO("TEST-7", "Задача John 2", "Open", "John Doe", nextMonth),
                new IssueViewDTO("TEST-8", "Задача John 3", "Open", "John Doe", tomorrow),
                new IssueViewDTO("TEST-9", "Задача John 4", "Open", "John Doe", tomorrow),
                new IssueViewDTO("TEST-10", "Задача John 5", "Open", "John Doe", nextMonth),
                new IssueViewDTO("TEST-11", "Задача John 6", "Open", "John Doe", nextMonth),
                new IssueViewDTO("TEST-12", "Задача John 7", "Open", "John Doe", nextMonth),
                new IssueViewDTO("TEST-13", "Задача John 8", "Open", "John Doe", nextMonth)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals("Должно быть 3 исполнителя", 3, result.size());

        Map<String, WorkloadViewDTO> workloadMap = new HashMap<>();
        for (WorkloadViewDTO w : result) {
            workloadMap.put(w.getAssignee(), w);
        }

        assertTrue("Должен быть Иван", workloadMap.containsKey("Иван Петров"));
        assertTrue("Должна быть Мария", workloadMap.containsKey("Мария Иванова"));
        assertTrue("Должен быть John", workloadMap.containsKey("John Doe"));

        assertEquals("Иван - low", "low", workloadMap.get("Иван Петров").getLoadLevel());
        assertEquals("Мария - critical", "critical", workloadMap.get("Мария Иванова").getLoadLevel());
        assertEquals("John - high", "high", workloadMap.get("John Doe").getLoadLevel());
    }

    @Test
    public void testAnalyzeWorkload_UnassignedIssues() {
        // Arrange
        String projectKey = "TEST";
        List<IssueViewDTO> issues = Arrays.asList(
                new IssueViewDTO("TEST-1", "Без исполнителя 1", "Open", "Unassigned", yesterday),
                new IssueViewDTO("TEST-2", "Без исполнителя 2", "Open", "Unassigned", tomorrow)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals("Unassigned тоже считается исполнителем", 1, result.size());
        WorkloadViewDTO workload = result.get(0);
        assertEquals("Unassigned", workload.getAssignee());
        assertEquals(2, workload.getTotalTasks());
        assertEquals(1, workload.getOverdueTasks());
        assertEquals(1, workload.getTasksDueWithin7Days());
    }

    @Test
    public void testAnalyzeWorkload_EdgeCase_Exactly7Days() {
        // Arrange
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Date exactly7Days = calendar.getTime();

        String projectKey = "TEST";
        List<IssueViewDTO> issues = Collections.singletonList(
                new IssueViewDTO("TEST-1", "Задача", "Open", "User", exactly7Days)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issues);

        // Act
        List<WorkloadViewDTO> result = workloadAnalysisService.analyzeWorkload(projectKey);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Дедлайн ровно через 7 дней должен быть в пределах 7 дней",
                1, result.get(0).getTasksDueWithin7Days());
    }

    // ==================== Граничные тесты ====================

    @Test
    public void testCalculateLoadLevel_LowToMediumBoundary() {
        String projectKey = "TEST";

        // 4 задачи, 1 срочная -> low
        List<IssueViewDTO> issuesLow = Arrays.asList(
                new IssueViewDTO("TEST-1", "Задача 1", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-2", "Задача 2", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-3", "Задача 3", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-4", "Задача 4", "Open", "User", in3Days)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issuesLow);
        List<WorkloadViewDTO> resultLow = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("low", resultLow.get(0).getLoadLevel());

        // 4 задачи, 2 срочных -> medium
        List<IssueViewDTO> issuesMedium = Arrays.asList(
                new IssueViewDTO("TEST-1", "Задача 1", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-2", "Задача 2", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-3", "Задача 3", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-4", "Задача 4", "Open", "User", in3Days)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issuesMedium);
        List<WorkloadViewDTO> resultMedium = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("medium", resultMedium.get(0).getLoadLevel());
    }

    @Test
    public void testCalculateLoadLevel_MediumToHighBoundary() {
        String projectKey = "TEST";

        // 7 задач, нет просроченных, 3 срочных -> medium
        List<IssueViewDTO> issuesMedium = Arrays.asList(
                new IssueViewDTO("TEST-1", "Задача 1", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-2", "Задача 2", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-3", "Задача 3", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-4", "Задача 4", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-5", "Задача 5", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-6", "Задача 6", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-7", "Задача 7", "Open", "User", tomorrow)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issuesMedium);
        List<WorkloadViewDTO> resultMedium = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("medium", resultMedium.get(0).getLoadLevel());

        // 7 задач, 4 срочных -> high
        List<IssueViewDTO> issuesHigh = Arrays.asList(
                new IssueViewDTO("TEST-1", "Задача 1", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-2", "Задача 2", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-3", "Задача 3", "Open", "User", nextMonth),
                new IssueViewDTO("TEST-4", "Задача 4", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-5", "Задача 5", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-6", "Задача 6", "Open", "User", tomorrow),
                new IssueViewDTO("TEST-7", "Задача 7", "Open", "User", tomorrow)
        );

        fakeIssueDataService.setOpenIssues(projectKey, issuesHigh);
        List<WorkloadViewDTO> resultHigh = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("high", resultHigh.get(0).getLoadLevel());
    }

    @Test
    public void testCalculateLoadLevel_HighToCriticalBoundary() {
        String projectKey = "TEST";

        // 10 задач, 2 просроченных -> high
        List<IssueViewDTO> issuesHigh = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            issuesHigh.add(new IssueViewDTO("TEST-" + i, "Задача " + i, "Open", "User", nextMonth));
        }
        issuesHigh.add(new IssueViewDTO("TEST-9", "Просрочено 1", "Open", "User", yesterday));
        issuesHigh.add(new IssueViewDTO("TEST-10", "Просрочено 2", "Open", "User", yesterday));

        fakeIssueDataService.setOpenIssues(projectKey, issuesHigh);
        List<WorkloadViewDTO> resultHigh = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("high", resultHigh.get(0).getLoadLevel());

        // 11 задач -> critical
        List<IssueViewDTO> issuesCritical = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            issuesCritical.add(new IssueViewDTO("TEST-" + i, "Задача " + i, "Open", "User", nextMonth));
        }

        fakeIssueDataService.setOpenIssues(projectKey, issuesCritical);
        List<WorkloadViewDTO> resultCritical = workloadAnalysisService.analyzeWorkload(projectKey);
        assertEquals("critical", resultCritical.get(0).getLoadLevel());
    }

    // ==================== Fake реализация IssueDataService ====================

    /**
     * Простая fake-реализация IssueDataService для тестов
     */
    private static class FakeIssueDataService implements IssueDataService {
        private final Map<String, List<IssueViewDTO>> projectIssues = new HashMap<>();
        private final Map<String, List<IssueViewDTO>> openProjectIssues = new HashMap<>();
        private final Map<String, IssueViewDTO> issuesByKey = new HashMap<>();

        public void setOpenIssues(String projectKey, List<IssueViewDTO> issues) {
            openProjectIssues.put(projectKey, issues);
        }

        @Override
        public List<IssueViewDTO> getIssuesForProject(String projectKey) {
            return projectIssues.getOrDefault(projectKey, Collections.emptyList());
        }

        @Override
        public List<IssueViewDTO> getOpenIssuesForProject(String projectKey) {
            return openProjectIssues.getOrDefault(projectKey, Collections.emptyList());
        }

        @Override
        public IssueViewDTO getIssueByKey(String issueKey) {
            return issuesByKey.get(issueKey);
        }
    }
}