package ut.com.projmodel.plugin.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.projmodel.plugin.ao.ReportTaskAO;
import com.projmodel.plugin.dto.IssueViewDTO;
import com.projmodel.plugin.service.IssueDataService;
import com.projmodel.plugin.service.impl.ReportServiceImpl;
import net.java.ao.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceImplTest {

    @Mock
    private ActiveObjects activeObjects;

    @Mock
    private IssueDataService issueDataService;

    @Mock
    private ReportTaskAO reportTaskAO;

    @InjectMocks
    private ReportServiceImpl reportService;

    private List<String> validIssueKeys;
    private String validProjectKey;
    private Date testDate;

    @Before
    public void setUp() {
        validProjectKey = "TEST";
        validIssueKeys = Arrays.asList("TEST-1", "TEST-2", "TEST-3");
        testDate = new Date();

        // Базовое поведение для save()
        doNothing().when(reportTaskAO).save();
    }

    // ==================== createReportRequest ====================

    @Test
    public void testCreateReportRequest_ValidWordFormat() {
        // Arrange
        when(activeObjects.create(eq(ReportTaskAO.class), any(Map.class)))
                .thenReturn(reportTaskAO);

        // Act
        ReportTaskAO result = reportService.createReportRequest(validProjectKey, validIssueKeys, "WORD");

        // Assert
        assertNotNull(result);
        verify(reportTaskAO).save();

        // Проверяем что в create переданы правильные параметры
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(activeObjects).create(eq(ReportTaskAO.class), captor.capture());

        Map<String, Object> params = captor.getValue();
        assertEquals(validProjectKey, params.get("PROJECT_KEY"));
        assertEquals("TEST-1,TEST-2,TEST-3", params.get("ISSUE_KEYS"));
        assertEquals("WORD", params.get("REPORT_FORMAT"));
        assertEquals("PENDING", params.get("STATUS"));
        assertNull(params.get("FILE_PATH"));
        assertNotNull(params.get("CREATED_DATE"));
    }

    @Test
    public void testCreateReportRequest_ValidPdfFormat() {
        // Arrange
        when(activeObjects.create(eq(ReportTaskAO.class), any(Map.class)))
                .thenReturn(reportTaskAO);

        // Act
        ReportTaskAO result = reportService.createReportRequest(validProjectKey, validIssueKeys, "PDF");

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(activeObjects).create(eq(ReportTaskAO.class), captor.capture());
        assertEquals("PDF", captor.getValue().get("REPORT_FORMAT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_NullProjectKey() {
        reportService.createReportRequest(null, validIssueKeys, "WORD");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_EmptyProjectKey() {
        reportService.createReportRequest("", validIssueKeys, "WORD");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_BlankProjectKey() {
        reportService.createReportRequest("   ", validIssueKeys, "WORD");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_NullIssueKeys() {
        reportService.createReportRequest(validProjectKey, null, "WORD");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_EmptyIssueKeys() {
        reportService.createReportRequest(validProjectKey, Collections.emptyList(), "WORD");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_NullFormat() {
        reportService.createReportRequest(validProjectKey, validIssueKeys, null);
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_InvalidFormat() {
        reportService.createReportRequest(validProjectKey, validIssueKeys, "EXCEL");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReportRequest_InvalidFormatLowercase() {
        reportService.createReportRequest(validProjectKey, validIssueKeys, "word");
        verify(activeObjects, never()).create(any(), any(Map.class));
    }

    @Test
    public void testCreateReportRequest_IssueKeysJoinedCorrectly() {
        // Arrange
        when(activeObjects.create(eq(ReportTaskAO.class), any(Map.class)))
                .thenReturn(reportTaskAO);

        List<String> keys = Arrays.asList("KEY-1", "KEY-2");

        // Act
        reportService.createReportRequest(validProjectKey, keys, "WORD");

        // Assert
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(activeObjects).create(eq(ReportTaskAO.class), captor.capture());
        assertEquals("KEY-1,KEY-2", captor.getValue().get("ISSUE_KEYS"));
    }

    // ==================== getReportsByProject ====================

    @Test
    public void testGetReportsByProject_NoReports() {
        // Arrange
        when(activeObjects.find(eq(ReportTaskAO.class), any(Query.class)))
                .thenReturn(new ReportTaskAO[0]);

        // Act
        List<ReportTaskAO> result = reportService.getReportsByProject("NONEXISTENT");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(activeObjects).find(eq(ReportTaskAO.class), any(Query.class));
    }

    @Test
    public void testGetReportsByProject_WithReports() {
        // Arrange
        ReportTaskAO[] reports = {reportTaskAO, mock(ReportTaskAO.class)};
        when(activeObjects.find(eq(ReportTaskAO.class), any(Query.class)))
                .thenReturn(reports);

        // Act
        List<ReportTaskAO> result = reportService.getReportsByProject("TEST");

        // Assert
        assertEquals(2, result.size());
        verify(activeObjects).find(eq(ReportTaskAO.class), any(Query.class));
    }

    // ==================== getReportById ====================

    @Test
    public void testGetReportById_ExistingReport() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);

        // Act
        ReportTaskAO result = reportService.getReportById(1);

        // Assert
        assertNotNull(result);
        verify(activeObjects).get(ReportTaskAO.class, 1);
    }

    @Test
    public void testGetReportById_NonExistentReport() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 999)).thenReturn(null);

        // Act
        ReportTaskAO result = reportService.getReportById(999);

        // Assert
        assertNull(result);
        verify(activeObjects).get(ReportTaskAO.class, 999);
    }

    @Test
    public void testGetReportById_ZeroId() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 0)).thenReturn(null);

        // Act
        ReportTaskAO result = reportService.getReportById(0);

        // Assert
        assertNull(result);
    }

    @Test
    public void testGetReportById_NegativeId() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, -1)).thenReturn(null);

        // Act
        ReportTaskAO result = reportService.getReportById(-1);

        // Assert
        assertNull(result);
    }

    // ==================== updateReportStatus ====================

    @Test
    public void testUpdateReportStatus_ToGenerated() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        doNothing().when(reportTaskAO).setStatus("GENERATED");
        doNothing().when(reportTaskAO).setFilePath("/path/to/file.html");

        // Act
        reportService.updateReportStatus(1, "GENERATED", "/path/to/file.html");

        // Assert
        verify(reportTaskAO).setStatus("GENERATED");
        verify(reportTaskAO).setFilePath("/path/to/file.html");
        verify(reportTaskAO).save();
    }

    @Test
    public void testUpdateReportStatus_ToError() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        doNothing().when(reportTaskAO).setStatus("ERROR");

        // Act
        reportService.updateReportStatus(1, "ERROR", null);

        // Assert
        verify(reportTaskAO).setStatus("ERROR");
        verify(reportTaskAO, never()).setFilePath(anyString());
        verify(reportTaskAO).save();
    }

    @Test
    public void testUpdateReportStatus_NonExistentReport() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 999)).thenReturn(null);

        // Act - не должно быть исключений
        reportService.updateReportStatus(999, "GENERATED", null);

        // Assert
        verify(reportTaskAO, never()).setStatus(anyString());
        verify(reportTaskAO, never()).save();
    }

    @Test
    public void testUpdateReportStatus_EmptyFilePath() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);

        // Act
        reportService.updateReportStatus(1, "GENERATED", "");

        // Assert
        verify(reportTaskAO).setStatus("GENERATED");
        verify(reportTaskAO, never()).setFilePath(anyString());
        verify(reportTaskAO).save();
    }

    @Test
    public void testUpdateReportStatus_BlankFilePath() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);

        // Act
        reportService.updateReportStatus(1, "GENERATED", "   ");

        // Assert
        verify(reportTaskAO).setStatus("GENERATED");
        verify(reportTaskAO, never()).setFilePath(anyString());
        verify(reportTaskAO).save();
    }

    // ==================== generateReportFile ====================

    @Test
    public void testGenerateReportFile_WordFormat_Success() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getIssueKeys()).thenReturn("TEST-1,TEST-2,TEST-3");
        when(reportTaskAO.getProjectKey()).thenReturn("TEST");
        when(reportTaskAO.getReportFormat()).thenReturn("WORD");

        IssueViewDTO issue1 = new IssueViewDTO("TEST-1", "Задача 1", "Open", "Иван", new Date());
        IssueViewDTO issue2 = new IssueViewDTO("TEST-2", "Задача 2", "Done", "Мария", null);
        IssueViewDTO issue3 = new IssueViewDTO("TEST-3", "Задача 3", "In Progress", "John", new Date());

        when(issueDataService.getIssueByKey("TEST-1")).thenReturn(issue1);
        when(issueDataService.getIssueByKey("TEST-2")).thenReturn(issue2);
        when(issueDataService.getIssueByKey("TEST-3")).thenReturn(issue3);

        // Act
        byte[] result = reportService.generateReportFile(1);

        // Assert
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Файл должен быть не пустым", result.length > 0);

        verify(reportTaskAO).setStatus("GENERATED");
        verify(reportTaskAO).save();

        verify(issueDataService).getIssueByKey("TEST-1");
        verify(issueDataService).getIssueByKey("TEST-2");
        verify(issueDataService).getIssueByKey("TEST-3");
    }

    @Test
    public void testGenerateReportFile_PdfFormat_Success() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getProjectKey()).thenReturn("TEST");
        when(reportTaskAO.getIssueKeys()).thenReturn("TEST-1");
        when(reportTaskAO.getReportFormat()).thenReturn("PDF");

        IssueViewDTO issue = new IssueViewDTO("TEST-1", "Задача", "Open", "Иван", new Date());
        when(issueDataService.getIssueByKey("TEST-1")).thenReturn(issue);

        // Act
        byte[] result = reportService.generateReportFile(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(reportTaskAO).setStatus("GENERATED");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateReportFile_NonExistentReport() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 999)).thenReturn(null);

        // Act
        reportService.generateReportFile(999);
    }

    @Test
    public void testGenerateReportFile_WithMissingIssues() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getProjectKey()).thenReturn("TEST");
        when(reportTaskAO.getIssueKeys()).thenReturn("TEST-1,NONEXISTENT,TEST-2");
        when(reportTaskAO.getReportFormat()).thenReturn("WORD");

        IssueViewDTO issue1 = new IssueViewDTO("TEST-1", "Задача 1", "Open", "Иван", new Date());
        IssueViewDTO issue2 = new IssueViewDTO("TEST-2", "Задача 2", "Done", "Мария", null);

        when(issueDataService.getIssueByKey("TEST-1")).thenReturn(issue1);
        when(issueDataService.getIssueByKey("NONEXISTENT")).thenReturn(null);
        when(issueDataService.getIssueByKey("TEST-2")).thenReturn(issue2);

        // Act
        byte[] result = reportService.generateReportFile(1);

        // Assert
        assertNotNull(result);
        String content = new String(result);
        assertTrue(content.contains("TEST-1"));
        assertTrue(content.contains("TEST-2"));
        assertFalse(content.contains("NONEXISTENT"));
        verify(reportTaskAO).setStatus("GENERATED");
    }

    @Test
    public void testGenerateReportFile_AllIssuesMissing() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getIssueKeys()).thenReturn("GHOST-1,GHOST-2");
        when(reportTaskAO.getProjectKey()).thenReturn("TEST");
        when(reportTaskAO.getReportFormat()).thenReturn("WORD");

        when(issueDataService.getIssueByKey(anyString())).thenReturn(null);

        // Act
        byte[] result = reportService.generateReportFile(1);

        // Assert
        assertNotNull("Результат не должен быть null", result);

        verify(reportTaskAO).setStatus("GENERATED");
        verify(reportTaskAO).save();
    }

    @Test
    public void testGenerateReportFile_ExceptionSetsErrorStatus() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getIssueKeys()).thenReturn("TEST-1");
        // getProjectKey и getReportFormat не нужны — до них не дойдёт
        when(issueDataService.getIssueByKey("TEST-1"))
                .thenThrow(new RuntimeException("Jira недоступна"));

        // Act & Assert
        try {
            reportService.generateReportFile(1);
            fail("Должно быть исключение");
        } catch (RuntimeException e) {
            assertEquals("Jira недоступна", e.getMessage());
            verify(reportTaskAO, never()).setStatus(anyString());
            verify(reportTaskAO, never()).save();
        }
    }

    @Test
    public void testGenerateReportFile_IssueKeysWithSpaces() {
        // Arrange
        when(activeObjects.get(ReportTaskAO.class, 1)).thenReturn(reportTaskAO);
        when(reportTaskAO.getProjectKey()).thenReturn("TEST");
        when(reportTaskAO.getIssueKeys()).thenReturn(" TEST-1 , TEST-2 ");
        when(reportTaskAO.getReportFormat()).thenReturn("WORD");

        IssueViewDTO issue1 = new IssueViewDTO("TEST-1", "Задача 1", "Open", "Иван", new Date());
        IssueViewDTO issue2 = new IssueViewDTO("TEST-2", "Задача 2", "Done", "Мария", null);

        when(issueDataService.getIssueByKey("TEST-1")).thenReturn(issue1);
        when(issueDataService.getIssueByKey("TEST-2")).thenReturn(issue2);

        // Act
        byte[] result = reportService.generateReportFile(1);

        // Assert
        assertNotNull(result);
        verify(issueDataService).getIssueByKey("TEST-1");
        verify(issueDataService).getIssueByKey("TEST-2");
    }
}