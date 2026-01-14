package com.ubs.expensemanager.service;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
import com.ubs.expensemanager.exception.BudgetExceededException;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.service.budget.DepartmentBudgetValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentBudgetValidationStrategyTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private DepartmentBudgetValidationStrategy strategy;

    @Captor
    private ArgumentCaptor<BudgetExceededEvent> eventCaptor;

    private User employee;
    private Department itDepartment;
    private ExpenseCategory foodCategory;
    private Currency usdCurrency;
    private Expense expense;

    @BeforeEach
    void setUp() {
        // Setup Currency
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

        // Setup User
        employee = User.builder()
                .id(1L)
                .name("John Employee")
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
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

        // Setup Expense
        expense = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50))
                .description("Team lunch")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("validate() - Monthly Budget Validation (Blocking)")
    class MonthlyBudgetValidationTests {

        @Test
        void validate_departmentIsNull_noValidationPerformed() {
            // Given
            User userWithNoDepartment = User.builder()
                    .id(2L)
                    .name("No Department User")
                    .email("nodept@ubs.com")
                    .role(UserRole.EMPLOYEE)
                    .department(null)
                    .active(true)
                    .build();

            Expense expenseWithNoDepartment = Expense.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(50))
                    .description("Team lunch")
                    .expenseDate(LocalDate.now())
                    .user(userWithNoDepartment)
                    .expenseCategory(foodCategory)
                    .currency(usdCurrency)
                    .status(ExpenseStatus.PENDING)
                    .build();

            // When
            strategy.validate(userWithNoDepartment.getId(), foodCategory, expenseWithNoDepartment, BigDecimal.valueOf(50));

            // Then
            verifyNoInteractions(expenseRepository);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void validate_monthlyBudgetNotExceeded_noExceptionThrown() {
            // Given
            BigDecimal currentMonthlyTotal = BigDecimal.valueOf(11000);
            BigDecimal newAmount = BigDecimal.valueOf(50);
            // 11000 + 50 = 11050, which is less than the monthly budget of 12000

            when(expenseRepository.sumAmountByDepartmentAndDateRangeExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId())))
                    .thenReturn(currentMonthlyTotal);

            // When & Then - no exception thrown
            assertDoesNotThrow(() ->
                strategy.validate(employee.getId(), foodCategory, expense, newAmount)
            );

            // Verify monthly budget was checked
            verify(expenseRepository).sumAmountByDepartmentAndDateRangeExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId()));

            // Verify no event was published (validate() only does monthly which throws, not publishes)
            verify(eventPublisher, never()).publishBudgetExceededEvent(any());
        }

        @Test
        void validate_monthlyBudgetExceeded_throwsException() {
            // Given
            BigDecimal currentMonthlyTotal = BigDecimal.valueOf(11980);
            BigDecimal newAmount = BigDecimal.valueOf(50);
            // 11980 + 50 = 12030, which exceeds the monthly budget of 12000

            when(expenseRepository.sumAmountByDepartmentAndDateRangeExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId())))
                    .thenReturn(currentMonthlyTotal);

            // When & Then
            BudgetExceededException exception = assertThrows(BudgetExceededException.class, () ->
                strategy.validate(employee.getId(), foodCategory, expense, newAmount)
            );

            // Verify exception details
            assertEquals("IT", exception.getDepartmentName());
            assertEquals(YearMonth.from(expense.getExpenseDate()), exception.getYearMonth());
            assertEquals(currentMonthlyTotal, exception.getCurrentTotal());
            assertEquals(0, new BigDecimal("12030.00").compareTo(exception.getNewTotal()));
            assertEquals(0, new BigDecimal("12000.00").compareTo(exception.getBudgetLimit()));

            // Verify no event was published (exception thrown instead)
            verify(eventPublisher, never()).publishBudgetExceededEvent(any());
        }

        @Test
        void validate_newExpenseWithNullId_usesNonExcludingQuery() {
            // Given - expense with null ID (new expense)
            Expense newExpense = Expense.builder()
                    .id(null)  // New expense has no ID yet
                    .amount(BigDecimal.valueOf(50))
                    .description("New expense")
                    .expenseDate(LocalDate.now())
                    .user(employee)
                    .expenseCategory(foodCategory)
                    .currency(usdCurrency)
                    .status(ExpenseStatus.PENDING)
                    .build();

            BigDecimal currentMonthlyTotal = BigDecimal.valueOf(11000);
            BigDecimal newAmount = BigDecimal.valueOf(50);

            when(expenseRepository.sumAmountByDepartmentAndDateRange(
                    eq(itDepartment.getId()), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(currentMonthlyTotal);

            // When & Then - no exception thrown
            assertDoesNotThrow(() ->
                strategy.validate(employee.getId(), foodCategory, newExpense, newAmount)
            );

            // Verify non-excluding query was used (no expenseId parameter)
            verify(expenseRepository).sumAmountByDepartmentAndDateRange(
                    eq(itDepartment.getId()), any(LocalDate.class), any(LocalDate.class));
            verify(expenseRepository, never()).sumAmountByDepartmentAndDateRangeExcludingExpense(
                    any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("validateDailyBudgetOnly() - Daily Budget Validation (Warning-Only)")
    class DailyBudgetValidationTests {

        @Test
        void validateDailyBudgetOnly_dailyBudgetNotExceeded_noEventPublished() {
            // Given
            BigDecimal currentDailyTotal = BigDecimal.valueOf(300);
            BigDecimal newAmount = BigDecimal.valueOf(50);
            // 300 + 50 = 350, which is less than the daily budget of 400

            when(expenseRepository.sumAmountByDepartmentAndDateExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), eq(expense.getId())))
                    .thenReturn(currentDailyTotal);

            // When
            strategy.validateDailyBudgetOnly(employee.getId(), foodCategory, expense, newAmount);

            // Then
            verify(expenseRepository).sumAmountByDepartmentAndDateExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), eq(expense.getId()));
            verify(eventPublisher, never()).publishBudgetExceededEvent(any());
        }

        @Test
        void validateDailyBudgetOnly_dailyBudgetExceeded_eventPublished() {
            // Given
            BigDecimal currentDailyTotal = BigDecimal.valueOf(360);
            BigDecimal newAmount = BigDecimal.valueOf(50);
            // 360 + 50 = 410, which exceeds the daily budget of 400

            when(expenseRepository.sumAmountByDepartmentAndDateExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), eq(expense.getId())))
                    .thenReturn(currentDailyTotal);

            // When
            strategy.validateDailyBudgetOnly(employee.getId(), foodCategory, expense, newAmount);

            // Then
            verify(expenseRepository).sumAmountByDepartmentAndDateExcludingExpense(
                    eq(itDepartment.getId()), any(LocalDate.class), eq(expense.getId()));
            verify(eventPublisher).publishBudgetExceededEvent(eventCaptor.capture());

            BudgetExceededEvent capturedEvent = eventCaptor.getValue();
            assertEquals(BudgetExceededEvent.BudgetType.DEPARTAMENT, capturedEvent.getBudgetType());
            assertEquals(expense, capturedEvent.getExpense());
            assertEquals(foodCategory, capturedEvent.getCategory());
            assertEquals(employee.getId(), capturedEvent.getUserId());
            assertEquals(currentDailyTotal, capturedEvent.getCurrentTotal());
            assertEquals(0, new BigDecimal("410.00").compareTo(capturedEvent.getNewTotal()));
            assertEquals(0, new BigDecimal("400.00").compareTo(capturedEvent.getBudgetLimit()));
            assertEquals(expense.getExpenseDate(), capturedEvent.getDate());
        }

        @Test
        void validateDailyBudgetOnly_departmentIsNull_noValidationPerformed() {
            // Given
            User userWithNoDepartment = User.builder()
                    .id(2L)
                    .name("No Department User")
                    .email("nodept@ubs.com")
                    .role(UserRole.EMPLOYEE)
                    .department(null)
                    .active(true)
                    .build();

            Expense expenseWithNoDepartment = Expense.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(50))
                    .description("Team lunch")
                    .expenseDate(LocalDate.now())
                    .user(userWithNoDepartment)
                    .expenseCategory(foodCategory)
                    .currency(usdCurrency)
                    .status(ExpenseStatus.PENDING)
                    .build();

            // When
            strategy.validateDailyBudgetOnly(userWithNoDepartment.getId(), foodCategory, expenseWithNoDepartment, BigDecimal.valueOf(50));

            // Then
            verifyNoInteractions(expenseRepository);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void validateDailyBudgetOnly_nullDailyBudget_noValidationPerformed() {
            // Given
            Department departmentWithNullDailyBudget = Department.builder()
                    .id(2L)
                    .name("HR")
                    .dailyBudget(null)  // No daily budget set
                    .monthlyBudget(BigDecimal.valueOf(12000))
                    .currency(usdCurrency)
                    .build();

            User hrEmployee = User.builder()
                    .id(2L)
                    .name("HR Employee")
                    .email("hr@ubs.com")
                    .role(UserRole.EMPLOYEE)
                    .department(departmentWithNullDailyBudget)
                    .active(true)
                    .build();

            Expense hrExpense = Expense.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(50))
                    .description("HR lunch")
                    .expenseDate(LocalDate.now())
                    .user(hrEmployee)
                    .expenseCategory(foodCategory)
                    .currency(usdCurrency)
                    .status(ExpenseStatus.PENDING)
                    .build();

            // When
            strategy.validateDailyBudgetOnly(hrEmployee.getId(), foodCategory, hrExpense, BigDecimal.valueOf(50));

            // Then - no daily budget validation performed
            verify(expenseRepository, never()).sumAmountByDepartmentAndDateExcludingExpense(any(), any(), any());
            verify(eventPublisher, never()).publishBudgetExceededEvent(any());
        }

        @Test
        void validateDailyBudgetOnly_newExpenseWithNullId_usesNonExcludingQuery() {
            // Given - expense with null ID (new expense)
            Expense newExpense = Expense.builder()
                    .id(null)  // New expense has no ID yet
                    .amount(BigDecimal.valueOf(50))
                    .description("New expense")
                    .expenseDate(LocalDate.now())
                    .user(employee)
                    .expenseCategory(foodCategory)
                    .currency(usdCurrency)
                    .status(ExpenseStatus.PENDING)
                    .build();

            BigDecimal currentDailyTotal = BigDecimal.valueOf(300);
            BigDecimal newAmount = BigDecimal.valueOf(50);

            when(expenseRepository.sumAmountByDepartmentAndDate(
                    eq(itDepartment.getId()), any(LocalDate.class)))
                    .thenReturn(currentDailyTotal);

            // When
            strategy.validateDailyBudgetOnly(employee.getId(), foodCategory, newExpense, newAmount);

            // Then - non-excluding query was used (no expenseId parameter)
            verify(expenseRepository).sumAmountByDepartmentAndDate(
                    eq(itDepartment.getId()), any(LocalDate.class));
            verify(expenseRepository, never()).sumAmountByDepartmentAndDateExcludingExpense(
                    any(), any(), any());
            verify(eventPublisher, never()).publishBudgetExceededEvent(any());
        }
    }
}
