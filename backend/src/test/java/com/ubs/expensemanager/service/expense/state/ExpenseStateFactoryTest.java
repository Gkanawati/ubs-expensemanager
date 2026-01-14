package com.ubs.expensemanager.service.expense.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ubs.expensemanager.model.ExpenseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseStateFactoryTest {

  private ExpenseStateFactory stateFactory;

  @BeforeEach
  void setUp() {
    PendingState pendingState = new PendingState();
    ApprovedByManagerState approvedByManagerState = new ApprovedByManagerState();
    ApprovedByFinanceState approvedByFinanceState = new ApprovedByFinanceState();
    RejectedState rejectedState = new RejectedState();

    stateFactory = new ExpenseStateFactory(
        pendingState,
        approvedByManagerState,
        approvedByFinanceState,
        rejectedState
    );

    stateFactory.initialize();
  }

  @Test
  void testGetPendingState() {
    ExpenseState state = stateFactory.getState(ExpenseStatus.PENDING);

    assertThat(state).isInstanceOf(PendingState.class);
    assertThat(state.getStatus()).isEqualTo(ExpenseStatus.PENDING);
  }

  @Test
  void testGetApprovedByManagerState() {
    ExpenseState state = stateFactory.getState(ExpenseStatus.APPROVED_BY_MANAGER);

    assertThat(state).isInstanceOf(ApprovedByManagerState.class);
    assertThat(state.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_MANAGER);
  }

  @Test
  void testGetApprovedByFinanceState() {
    ExpenseState state = stateFactory.getState(ExpenseStatus.APPROVED_BY_FINANCE);

    assertThat(state).isInstanceOf(ApprovedByFinanceState.class);
    assertThat(state.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_FINANCE);
  }

  @Test
  void testGetRejectedState() {
    ExpenseState state = stateFactory.getState(ExpenseStatus.REJECTED);

    assertThat(state).isInstanceOf(RejectedState.class);
    assertThat(state.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
  }

  @Test
  void testStateInstanceIsSingleton() {
    ExpenseState state1 = stateFactory.getState(ExpenseStatus.PENDING);
    ExpenseState state2 = stateFactory.getState(ExpenseStatus.PENDING);

    assertThat(state1).isSameAs(state2);
  }

  @Test
  void testGetStateWithNullStatus() {
    assertThatThrownBy(() -> stateFactory.getState(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No state found for status: null");
  }
}
