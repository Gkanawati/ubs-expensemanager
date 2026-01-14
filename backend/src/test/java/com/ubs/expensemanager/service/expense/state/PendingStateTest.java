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
@DisplayName("PendingState Tests")
class PendingStateTest {

  @Mock
  private ExpenseRepository expenseRepository;

  private PendingState pendingState;
  private Expense expense;
  private User manager;
  private User employee;
  private User finance;

  @BeforeEach
  void setUp() {
    pendingState = new PendingState();

    Department department = Department.builder()
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
        .status(ExpenseStatus.PENDING)
        .user(employee)
        .build();
  }

  @Test
  @DisplayName("getStatus() should return PENDING")
  void testGetStatus() {
    assertThat(pendingState.getStatus()).isEqualTo(ExpenseStatus.PENDING);
  }

  @Test
  @DisplayName("getValidTransitions() should return APPROVED_BY_MANAGER and REJECTED")
  void testGetValidTransitions() {
    Set<ExpenseStatus> validTransitions = pendingState.getValidTransitions();

    assertThat(validTransitions)
        .hasSize(2)
        .contains(ExpenseStatus.APPROVED_BY_MANAGER, ExpenseStatus.REJECTED);
  }

  @Test
  @DisplayName("canTransitionTo() should allow APPROVED_BY_MANAGER")
  void testCanTransitionToApprovedByManager() {
    assertThat(pendingState.canTransitionTo(ExpenseStatus.APPROVED_BY_MANAGER)).isTrue();
  }

  @Test
  @DisplayName("canTransitionTo() should allow REJECTED")
  void testCanTransitionToRejected() {
    assertThat(pendingState.canTransitionTo(ExpenseStatus.REJECTED)).isTrue();
  }

  @Test
  @DisplayName("canTransitionTo() should not allow APPROVED_BY_FINANCE")
  void testCannotTransitionToApprovedByFinance() {
    assertThat(pendingState.canTransitionTo(ExpenseStatus.APPROVED_BY_FINANCE)).isFalse();
  }

  @Test
  @DisplayName("canTransitionTo() should not allow PENDING")
  void testCannotTransitionToPending() {
    assertThat(pendingState.canTransitionTo(ExpenseStatus.PENDING)).isFalse();
  }

  @Test
  @DisplayName("approve() with MANAGER role and same department should transition to APPROVED_BY_MANAGER")
  void testApproveByManagerSuccess() {
    when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    pendingState.approve(context);

    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_MANAGER);
    verify(expenseRepository).save(expense);
  }

  @Test
  @DisplayName("approve() with MANAGER from different department should throw UnauthorizedExpenseAccessException")
  void testApproveByManagerDifferentDepartment() {
    Department otherDepartment = Department.builder()
        .id(2L)
        .name("Sales")
        .build();

    User managerOtherDept = User.builder()
        .id(4L)
        .email("manager2@test.com")
        .role(UserRole.MANAGER)
        .department(otherDepartment)
        .build();

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(managerOtherDept)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.approve(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("You can only process expenses for employees in your department");
  }

  @Test
  @DisplayName("approve() with FINANCE role should throw UnauthorizedExpenseAccessException")
  void testApproveByFinanceFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.approve(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only managers can approve pending expenses");
  }

  @Test
  @DisplayName("approve() with EMPLOYEE role should throw UnauthorizedExpenseAccessException")
  void testApproveByEmployeeFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(employee)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.approve(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only managers can approve pending expenses");
  }

  @Test
  @DisplayName("reject() with MANAGER role and same department should transition to REJECTED")
  void testRejectByManagerSuccess() {
    when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    pendingState.reject(context);

    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
    verify(expenseRepository).save(expense);
  }

  @Test
  @DisplayName("reject() with MANAGER from different department should throw UnauthorizedExpenseAccessException")
  void testRejectByManagerDifferentDepartment() {
    Department otherDepartment = Department.builder()
        .id(2L)
        .name("Sales")
        .build();

    User managerOtherDept = User.builder()
        .id(4L)
        .email("manager2@test.com")
        .role(UserRole.MANAGER)
        .department(otherDepartment)
        .build();

    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(managerOtherDept)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.reject(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("You can only process expenses for employees in your department");
  }

  @Test
  @DisplayName("reject() with FINANCE role should throw UnauthorizedExpenseAccessException")
  void testRejectByFinanceFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.reject(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only managers can reject pending expenses");
  }

  @Test
  @DisplayName("reject() with EMPLOYEE role should throw UnauthorizedExpenseAccessException")
  void testRejectByEmployeeFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(employee)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> pendingState.reject(context))
        .isInstanceOf(UnauthorizedExpenseAccessException.class)
        .hasMessageContaining("Only managers can reject pending expenses");
  }
}
