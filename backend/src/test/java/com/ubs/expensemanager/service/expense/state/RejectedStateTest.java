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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectedState Tests")
class RejectedStateTest {

  @Mock
  private ExpenseRepository expenseRepository;

  private RejectedState rejectedState;
  private Expense expense;
  private User manager;
  private Department department;

  @BeforeEach
  void setUp() {
    rejectedState = new RejectedState();

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

    manager = User.builder()
        .id(2L)
        .email("manager@test.com")
        .role(UserRole.MANAGER)
        .department(department)
        .build();

    expense = Expense.builder()
        .id(100L)
        .status(ExpenseStatus.REJECTED)
        .user(employee)
        .build();
  }

  @Test
  @DisplayName("getStatus() should return REJECTED")
  void testGetStatus() {
    assertThat(rejectedState.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
  }

  @Test
  @DisplayName("getValidTransitions() should return empty set")
  void testGetValidTransitions() {
    assertThat(rejectedState.getValidTransitions()).isEmpty();
  }

  @Test
  @DisplayName("canTransitionTo() should return false for all statuses")
  void testCannotTransitionToAnyStatus() {
    assertThat(rejectedState.canTransitionTo(ExpenseStatus.PENDING)).isFalse();
    assertThat(rejectedState.canTransitionTo(ExpenseStatus.APPROVED_BY_MANAGER)).isFalse();
    assertThat(rejectedState.canTransitionTo(ExpenseStatus.APPROVED_BY_FINANCE)).isFalse();
    assertThat(rejectedState.canTransitionTo(ExpenseStatus.REJECTED)).isFalse();
  }

  @Test
  @DisplayName("approve() should throw InvalidStatusTransitionException")
  void testApproveFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> rejectedState.approve(context))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("Cannot approve expense with terminal status REJECTED");
  }

  @Test
  @DisplayName("reject() should throw InvalidStatusTransitionException")
  void testRejectFails() {
    StateContext context = StateContext.builder()
        .expense(expense)
        .currentUser(manager)
        .expenseRepository(expenseRepository)
        .build();

    assertThatThrownBy(() -> rejectedState.reject(context))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("Cannot reject expense with terminal status REJECTED");
  }
}
