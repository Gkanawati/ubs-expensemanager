package com.ubs.expensemanager.service.expense.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApprovedByFinanceStateTest {

  @Mock
  private ExpenseRepository expenseRepository;

  private ApprovedByFinanceState approvedByFinanceState;
  private Expense expense;
  private User finance;
  private Department department;

  @BeforeEach
  void setUp() {
    approvedByFinanceState = new ApprovedByFinanceState();

    department = Department.builder()
        .id(1L)
        .name("Engineering")
        .build();

    User employee = User.builder()
        .id(1L)
        .email("employee@test.com")
        .role(UserRole.EMPLOYEE)
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
        .status(ExpenseStatus.APPROVED_BY_FINANCE)
        .user(employee)
        .build();
  }

  @Test
  void testGetStatus() {
    assertThat(approvedByFinanceState.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_FINANCE);
  }

  @Test
  void testGetValidTransitions() {
    assertThat(approvedByFinanceState.getValidTransitions()).isEmpty();
  }

  @Test
  void testCannotTransitionToAnyStatus() {
    assertThat(approvedByFinanceState.canTransitionTo(ExpenseStatus.PENDING)).isFalse();
    assertThat(approvedByFinanceState.canTransitionTo(ExpenseStatus.APPROVED_BY_MANAGER)).isFalse();
    assertThat(approvedByFinanceState.canTransitionTo(ExpenseStatus.APPROVED_BY_FINANCE)).isFalse();
    assertThat(approvedByFinanceState.canTransitionTo(ExpenseStatus.REJECTED)).isFalse();
  }

  @Test
  void testApproveFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByFinanceState.approve(context))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("Cannot approve expense with terminal status APPROVED_BY_FINANCE");
  }

  @Test
  void testRejectFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(finance)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> approvedByFinanceState.reject(context))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("Cannot reject expense with terminal status APPROVED_BY_FINANCE");
  }
}
