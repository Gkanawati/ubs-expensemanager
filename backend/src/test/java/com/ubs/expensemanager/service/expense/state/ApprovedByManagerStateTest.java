package com.ubs.expensemanager.service.expense.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.ExpenseRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovedByManagerState Tests")
class ApprovedByManagerStateTest {

  @Mock
  private ExpenseRepository expenseRepository;

  private ApprovedByManagerState approvedByManagerState;
  private Expense expense;
  private User manager;
  private User employee;
  private User finance;
  private Department department;

  @BeforeEach
  void setUp() {
    approvedByManagerState = new ApprovedByManagerState();

    department = Department.builder()
        .id(1L)
        .name("Engineering")
        .build();

    employee = User.builder()
        .id(1L)
        .email("employee@test.com")
        .role(UserRole.EMPLOYEE)
        .department(department)
        .build();

    manager = User.builder()
        .id(2L)
        .email("manager@test.com")
        .role(UserRole.MANAGER)
        .department(department)
        .build();

    finance = User.builder()
        .id(3L)
        .email("finance@test.com")
        .role(UserRole.FINANCE)
        .department(department)
        .build();

    expense = Expense.builder()
        .id(100L)
        .status(ExpenseStatus.APPROVED_BY_MANAGER)
        .user(employee)
        .build();
  }

  @Test
  @DisplayName("getStatus() should return APPROVED_BY_MANAGER")
  void testGetStatus() {
    assertThat(approvedByManagerState.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_MANAGER);
  }

  @Test
  @DisplayName("getValidTransitions() should return APPROVED_BY_FINANCE and REJECTED")
  void testGetValidTransitions() {
    Set<ExpenseStatus> validTransitions = approvedByManagerState.getValidTransitions();

    assertThat(validTransitions)
        .hasSize(2)
        .contains(ExpenseStatus.APPROVED_BY_FINANCE, ExpenseStatus.REJECTED);
  }

  @Test
  @DisplayName("canTransitionTo() should allow APPROVED_BY_FINANCE")
  void testCanTransitionToApprovedByFinance() {
    assertThat(approvedByManagerState.canTransitionTo(ExpenseStatus.APPROVED_BY_FINANCE)).isTrue();
  }

  @Test
  @DisplayName("canTransitionTo() should allow REJECTED")
  void testCanTransitionToRejected() {
    assertThat(approvedByManagerState.canTransitionTo(ExpenseStatus.REJECTED)).isTrue();
  }

  @Test
  @DisplayName("canTransitionTo() should not allow PENDING")
  void testCannotTransitionToPending() {
    assertThat(approvedByManagerState.canTransitionTo(ExpenseStatus.PENDING)).isFalse();
  }

  @Test
  @DisplayName("canTransitionTo() should not allow APPROVED_BY_MANAGER")
  void testCannotTransitionToApprovedByManager() {
    assertThat(approvedByManagerState.canTransitionTo(ExpenseStatus.APPROVED_BY_MANAGER)).isFalse();
  }

  @Test
  @DisplayName("approve() with FINANCE role should transition to APPROVED_BY_FINANCE")
  void testApproveByFinanceSuccess() {
    when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    approvedByManagerState.approve(context);

    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_FINANCE);
    verify(expenseRepository).save(expense);
  }

  @Test
  @DisplayName("approve() with MANAGER role should throw UnauthorizedExpenseAccessException")
  void testApproveByManagerFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByManagerState.approve(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only finance users can approve expenses at this stage");
  }

  @Test
  @DisplayName("approve() with EMPLOYEE role should throw UnauthorizedExpenseAccessException")
  void testApproveByEmployeeFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(employee)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByManagerState.approve(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only finance users can approve expenses at this stage");
  }

  @Test
  @DisplayName("reject() with FINANCE role should transition to REJECTED")
  void testRejectByFinanceSuccess() {
    when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    approvedByManagerState.reject(context);

    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
    verify(expenseRepository).save(expense);
  }

  @Test
  @DisplayName("reject() with MANAGER role should throw UnauthorizedExpenseAccessException")
  void testRejectByManagerFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByManagerState.reject(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only finance users can reject expenses at this stage");
  }

  @Test
  @DisplayName("reject() with EMPLOYEE role should throw UnauthorizedExpenseAccessException")
  void testRejectByEmployeeFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(employee)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByManagerState.reject(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only finance users can reject expenses at this stage");
  }
}
