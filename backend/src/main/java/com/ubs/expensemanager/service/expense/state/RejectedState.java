package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.model.ExpenseStatus;
import org.springframework.stereotype.Component;

/**
 * Terminal state representing expenses that have been rejected.
 *
 * <p>This is a final state in the approval workflow. Once an expense is rejected
 * (either by manager or finance), it cannot be modified or transitioned to any other state.</p>
 *
 * <p>Any attempts to approve or reject expenses in this state will throw
 * {@link InvalidStatusTransitionException}.</p>
 */
@Component
public class RejectedState extends TerminalState {

  /**
   * Constructs the REJECTED terminal state.
   */
  public RejectedState() {
    super(ExpenseStatus.REJECTED);
  }
}
