package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.model.ExpenseStatus;
import org.springframework.stereotype.Component;

/**
 * Terminal state representing expenses that have been fully approved by finance.
 *
 * <p>This is a final state in the approval workflow. Once an expense reaches
 * APPROVED_BY_FINANCE status, it cannot be modified or transitioned to any other state.</p>
 *
 * <p>Any attempts to approve or reject expenses in this state will throw
 * {@link InvalidStatusTransitionException}.</p>
 */
@Component
public class ApprovedByFinanceState extends TerminalState {

  /**
   * Constructs the APPROVED_BY_FINANCE terminal state.
   */
  public ApprovedByFinanceState() {
    super(ExpenseStatus.APPROVED_BY_FINANCE);
  }
}
