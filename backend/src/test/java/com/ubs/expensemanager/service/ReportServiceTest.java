package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.DepartmentExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.PersonalExpenseSummaryResponse;
import com.ubs.expensemanager.model.*;
import com.ubs.expensemanager.repository.DepartmentRepository;
import com.ubs.expensemanager.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    ExpenseRepository expenseRepository;

    @Mock
    DepartmentRepository departmentRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @InjectMocks
    ReportService reportService;

    User employee;
    User manager;
    Department itDepartment;
    Currency usdCurrency;
    ExpenseCategory foodCategory;
    Expense expense1;
    Expense expense2;
    Expense expense3;

    @BeforeEach
    void setUp() {
        // Setup Currency first (needed by Department)
        usdCurrency = Currency.builder()
                .id(1L)
                .name("USD")
                .exchangeRate(BigDecimal.ONE)
                .build();

        // Setup Department
        itDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .dailyBudget(BigDecimal.valueOf(400))
                .monthlyBudget(BigDecimal.valueOf(12000))
                .currency(usdCurrency)
                .build();

        // Setup Users
        employee = User.builder()
                .id(1L)
                .name("John Employee")
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
                .department(itDepartment)
                .active(true)
                .build();

        manager = User.builder()
                .id(2L)
                .name("Jane Manager")
                .email("manager@ubs.com")
                .role(UserRole.MANAGER)
                .department(itDepartment)
                .active(true)
                .build();

        // Setup ExpenseCategory
        foodCategory = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(BigDecimal.valueOf(100))
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency(usdCurrency)
                .build();

        // Setup Expenses
        expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50))
                .description("Team lunch")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.APPROVED_BY_FINANCE)
                .build();

        expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(75))
                .description("Client dinner")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.PENDING)
                .build();

        expense3 = Expense.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(100))
                .description("Office supplies")
                .expenseDate(LocalDate.now().minusDays(1))
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.APPROVED_BY_MANAGER)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== EXPENSES BY EMPLOYEE REPORT TESTS ====================

    @Test
    void getExpensesByEmployeeReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1, expense2, expense3));

        List<EmployeeExpenseReportResponse> result = reportService.getExpensesByEmployeeReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("John Employee", result.getFirst().getEmployee()),
                () -> assertEquals(new BigDecimal("225.00"), result.getFirst().getTotal()),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED)
        );
    }

    @Test
    void getExpensesByEmployeeReport_withNullDates_usesDefaults() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(expense1));

        List<EmployeeExpenseReportResponse> result = reportService.getExpensesByEmployeeReport(null, null);

        assertNotNull(result);
        verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(any(LocalDate.class), any(LocalDate.class), eq(ExpenseStatus.REJECTED));
    }

    // ==================== EXPENSES BY CATEGORY REPORT TESTS ====================

    @Test
    void getExpensesByCategoryReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1, expense2, expense3));

        List<CategoryExpenseReportResponse> result = reportService.getExpensesByCategoryReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Food", result.getFirst().getCategory()),
                () -> assertEquals(new BigDecimal("225.00"), result.getFirst().getTotal()),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED)
        );
    }

    // ==================== EXPENSES BY DEPARTMENT REPORT TESTS ====================

    @Test
    void getExpensesByDepartmentReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(expense1, expense2, expense3));
        when(departmentRepository.findAll()).thenReturn(List.of(itDepartment));

        List<DepartmentExpenseReportResponse> result = reportService.getExpensesByDepartmentReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("IT", result.getFirst().getDepartment()),
                () -> assertEquals(new BigDecimal("225.00"), result.getFirst().getUsed()),
                () -> assertTrue(result.getFirst().getRemaining().compareTo(BigDecimal.ZERO) > 0),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(any(), any(), eq(ExpenseStatus.REJECTED)),
                () -> verify(departmentRepository).findAll()
        );
    }

    // ==================== CSV GENERATION TESTS ====================

    @Test
    void getExpensesByEmployeeCsvReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1));

        String csv = reportService.getExpensesByEmployeeCsvReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(csv),
                () -> assertTrue(csv.contains("Employee,Total (USD)")),
                () -> assertTrue(csv.contains("John Employee")),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED)
        );
    }

    @Test
    void getExpensesByCategoryCsvReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1));

        String csv = reportService.getExpensesByCategoryCsvReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(csv),
                () -> assertTrue(csv.contains("Category,Total (USD)")),
                () -> assertTrue(csv.contains("Food")),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(startDate, endDate, ExpenseStatus.REJECTED)
        );
    }

    @Test
    void getExpensesByDepartmentCsvReport_success() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(expense1));
        when(departmentRepository.findAll()).thenReturn(List.of(itDepartment));

        String csv = reportService.getExpensesByDepartmentCsvReport(startDate, endDate);

        assertAll(
                () -> assertNotNull(csv),
                () -> assertTrue(csv.contains("Department,Used (USD),Remaining (USD),Over Budget (USD)")),
                () -> assertTrue(csv.contains("IT")),
                () -> verify(expenseRepository).findAllByExpenseDateBetweenAndStatusNot(any(), any(), eq(ExpenseStatus.REJECTED)),
                () -> verify(departmentRepository).findAll()
        );
    }

    @Test
    void generateCsvFilename_success() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        String filename = reportService.generateCsvFilename("test-report", startDate, endDate);

        assertAll(
                () -> assertNotNull(filename),
                () -> assertTrue(filename.contains("test-report")),
                () -> assertTrue(filename.contains("2026-01-01")),
                () -> assertTrue(filename.contains("2026-01-31")),
                () -> assertTrue(filename.endsWith(".csv"))
        );
    }

    // ==================== EXPENSE SUMMARY TESTS ====================

    @Test
    void getExpenseSummary_asEmployee_returnsPersonalSummary() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(employee);
        when(expenseRepository.findAllByUserIdAndStatusNot(1L, ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1, expense2, expense3));
        when(expenseRepository.findTopByUserIdAndStatusNotOrderByExpenseDateDesc(
                eq(1L), eq(ExpenseStatus.REJECTED), any(PageRequest.class)))
                .thenReturn(List.of(expense1, expense2, expense3));
        when(expenseRepository.findAllByUserIdAndExpenseDateBetweenAndStatusNot(
                eq(1L), any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(expense1, expense2));

        PersonalExpenseSummaryResponse result = reportService.getExpenseSummary();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(new BigDecimal("225.00"), result.getTotalExpenses()),
                () -> assertEquals(1, result.getApprovedExpensesCount()),
                () -> assertEquals(2, result.getPendingExpensesCount()),
                () -> assertEquals(new BigDecimal("125.00"), result.getExpensesThisMonth()),
                () -> assertEquals(3, result.getLastExpenses().size()),
                () -> verify(expenseRepository).findAllByUserIdAndStatusNot(1L, ExpenseStatus.REJECTED)
        );
    }

    @Test
    void getExpenseSummary_asManager_returnsOverallSummary() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(manager);
        when(expenseRepository.findAllByStatusNot(ExpenseStatus.REJECTED))
                .thenReturn(List.of(expense1, expense2, expense3));
        when(expenseRepository.findTopByStatusNotOrderByExpenseDateDesc(
                eq(ExpenseStatus.REJECTED), any(PageRequest.class)))
                .thenReturn(List.of(expense1, expense2, expense3));
        when(expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(expense1, expense2));

        PersonalExpenseSummaryResponse result = reportService.getExpenseSummary();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(new BigDecimal("225.00"), result.getTotalExpenses()),
                () -> assertEquals(1, result.getApprovedExpensesCount()),
                () -> assertEquals(2, result.getPendingExpensesCount()),
                () -> assertEquals(new BigDecimal("125.00"), result.getExpensesThisMonth()),
                () -> assertEquals(3, result.getLastExpenses().size()),
                () -> verify(expenseRepository).findAllByStatusNot(ExpenseStatus.REJECTED)
        );
    }

    @Test
    void getExpenseSummary_countsCorrectStatuses() {
        Expense approvedExpense = Expense.builder()
                .id(4L)
                .amount(BigDecimal.valueOf(200))
                .description("Approved expense")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.APPROVED_BY_FINANCE)
                .build();

        Expense pendingExpense = Expense.builder()
                .id(5L)
                .amount(BigDecimal.valueOf(150))
                .description("Pending expense")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.PENDING)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(employee);
        when(expenseRepository.findAllByUserIdAndStatusNot(1L, ExpenseStatus.REJECTED))
                .thenReturn(List.of(approvedExpense, pendingExpense));
        when(expenseRepository.findTopByUserIdAndStatusNotOrderByExpenseDateDesc(
                eq(1L), eq(ExpenseStatus.REJECTED), any(PageRequest.class)))
                .thenReturn(List.of(approvedExpense));
        when(expenseRepository.findAllByUserIdAndExpenseDateBetweenAndStatusNot(
                eq(1L), any(), any(), eq(ExpenseStatus.REJECTED)))
                .thenReturn(List.of(approvedExpense));

        PersonalExpenseSummaryResponse result = reportService.getExpenseSummary();

        assertAll(
                () -> assertEquals(1, result.getApprovedExpensesCount()),
                () -> assertEquals(1, result.getPendingExpensesCount())
        );
    }
}
