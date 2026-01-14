package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.DepartmentExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.LastExpenseDto;
import com.ubs.expensemanager.dto.response.PersonalExpenseSummaryResponse;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.ReportService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ReportController}.
 *
 * <p>Tests expense report endpoints using {@link MockMvc}.
 * {@link ReportService} is mocked to isolate controller behavior.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    private static final String BASE_URL = "/api/reports";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private EmployeeExpenseReportResponse employeeReport;
    private CategoryExpenseReportResponse categoryReport;
    private DepartmentExpenseReportResponse departmentReport;
    private PersonalExpenseSummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        employeeReport = EmployeeExpenseReportResponse.builder()
                .employee("John Doe")
                .total(new BigDecimal("3500.50"))
                .build();

        categoryReport = CategoryExpenseReportResponse.builder()
                .category("Food")
                .total(new BigDecimal("1200.00"))
                .build();

        departmentReport = DepartmentExpenseReportResponse.builder()
                .department("Engineering")
                .used(new BigDecimal("5000.00"))
                .remaining(new BigDecimal("3000.00"))
                .overBudget(BigDecimal.ZERO)
                .build();

        LastExpenseDto lastExpense = LastExpenseDto.builder()
                .description("Team lunch")
                .date(LocalDate.of(2026, 1, 10))
                .status(ExpenseStatus.PENDING)
                .build();

        summaryResponse = PersonalExpenseSummaryResponse.builder()
                .totalExpenses(new BigDecimal("5420.75"))
                .approvedExpensesCount(15)
                .pendingExpensesCount(3)
                .expensesThisMonth(new BigDecimal("720.00"))
                .lastExpenses(List.of(lastExpense))
                .build();
    }

    @Test
    void getExpensesByEmployee_success_returnsOk() throws Exception {
        when(reportService.getExpensesByEmployeeReport(any(), any()))
                .thenReturn(List.of(employeeReport));

        mockMvc.perform(get(BASE_URL + "/expenses/by-employee")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee").value("John Doe"))
                .andExpect(jsonPath("$[0].total").value(3500.50));

        verify(reportService).getExpensesByEmployeeReport(any(), any());
    }

    @Test
    void getExpensesByEmployee_noDateParams_returnsOk() throws Exception {
        when(reportService.getExpensesByEmployeeReport(null, null))
                .thenReturn(List.of(employeeReport));

        mockMvc.perform(get(BASE_URL + "/expenses/by-employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee").value("John Doe"));

        verify(reportService).getExpensesByEmployeeReport(null, null);
    }

    @Test
    void getExpensesByEmployeeCsv_success_returnsOk() throws Exception {
        String csvContent = "Employee,Total (USD)\nJohn Doe,3500.50\n";
        when(reportService.getExpensesByEmployeeCsvReport(any(), any()))
                .thenReturn(csvContent);
        when(reportService.generateCsvFilename(anyString(), any(), any()))
                .thenReturn("expenses-by-employee_2026-01-01_to_2026-01-31.csv");

        mockMvc.perform(get(BASE_URL + "/expenses/by-employee/csv")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"expenses-by-employee_2026-01-01_to_2026-01-31.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvContent));

        verify(reportService).getExpensesByEmployeeCsvReport(any(), any());
        verify(reportService).generateCsvFilename(anyString(), any(), any());
    }

    @Test
    void getExpensesByCategory_success_returnsOk() throws Exception {
        when(reportService.getExpensesByCategoryReport(any(), any()))
                .thenReturn(List.of(categoryReport));

        mockMvc.perform(get(BASE_URL + "/expenses/by-category")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].total").value(1200.00));

        verify(reportService).getExpensesByCategoryReport(any(), any());
    }

    @Test
    void getExpensesByCategoryCsv_success_returnsOk() throws Exception {
        String csvContent = "Category,Total (USD)\nFood,1200.00\n";
        when(reportService.getExpensesByCategoryCsvReport(any(), any()))
                .thenReturn(csvContent);
        when(reportService.generateCsvFilename(anyString(), any(), any()))
                .thenReturn("expenses-by-category_2026-01-01_to_2026-01-31.csv");

        mockMvc.perform(get(BASE_URL + "/expenses/by-category/csv")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"expenses-by-category_2026-01-01_to_2026-01-31.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvContent));

        verify(reportService).getExpensesByCategoryCsvReport(any(), any());
        verify(reportService).generateCsvFilename(anyString(), any(), any());
    }

    @Test
    void getExpensesByDepartment_success_returnsOk() throws Exception {
        when(reportService.getExpensesByDepartmentReport(any(), any()))
                .thenReturn(List.of(departmentReport));

        mockMvc.perform(get(BASE_URL + "/department/budgets-vs-expenses")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Engineering"))
                .andExpect(jsonPath("$[0].used").value(5000.00))
                .andExpect(jsonPath("$[0].remaining").value(3000.00))
                .andExpect(jsonPath("$[0].overBudget").value(0));

        verify(reportService).getExpensesByDepartmentReport(any(), any());
    }

    @Test
    void getExpensesByDepartmentCsv_success_returnsOk() throws Exception {
        String csvContent = "Department,Used (USD),Remaining (USD),Over Budget (USD)\nEngineering,5000.00,3000.00,0.00\n";
        when(reportService.getExpensesByDepartmentCsvReport(any(), any()))
                .thenReturn(csvContent);
        when(reportService.generateCsvFilename(anyString(), any(), any()))
                .thenReturn("expenses-by-department_2026-01-01_to_2026-01-31.csv");

        mockMvc.perform(get(BASE_URL + "/department/budgets-vs-expenses/csv")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"expenses-by-department_2026-01-01_to_2026-01-31.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvContent));

        verify(reportService).getExpensesByDepartmentCsvReport(any(), any());
        verify(reportService).generateCsvFilename(anyString(), any(), any());
    }

    @Test
    void getExpenseSummary_success_returnsOk() throws Exception {
        when(reportService.getExpenseSummary())
                .thenReturn(summaryResponse);

        mockMvc.perform(get(BASE_URL + "/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(5420.75))
                .andExpect(jsonPath("$.approvedExpensesCount").value(15))
                .andExpect(jsonPath("$.pendingExpensesCount").value(3))
                .andExpect(jsonPath("$.expensesThisMonth").value(720.00))
                .andExpect(jsonPath("$.lastExpenses[0].description").value("Team lunch"))
                .andExpect(jsonPath("$.lastExpenses[0].status").value("PENDING"));

        verify(reportService).getExpenseSummary();
    }
}
