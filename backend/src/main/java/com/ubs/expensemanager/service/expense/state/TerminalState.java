package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.model.ExpenseStatus;
import java.util.Collections;

/**
 * Abstract base for terminal states that do not allow further transitions. Both APPROVED_BY_FINANCE
 * and REJECTED are terminal states.
 *
 * <p>Terminal states represent the end of the approval workflow. Once an
 * expense reaches a terminal state, it cannot be modified further through the approval/rejection
 * process.</p>
 *
 * <p>This class provides a default implementation that throws exceptions
 * for any approve() or reject() attempts.</p>
 */
public abstract class TerminalState extends AbstractExpenseState {

  /**
   * Constructs a new terminal state.
   *
   * @param status the expense status this terminal state represents
   */
  protected TerminalState(ExpenseStatus status) {
    super(status, Collections.emptySet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void approve(StateContext context) {
    throw new InvalidStatusTransitionException(
        String.format("Cannot approve expense with terminal status %s", getStatus())
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reject(StateContext context) {
    throw new InvalidStatusTransitionException(
        String.format("Cannot reject expense with terminal status %s", getStatus())
    );
  }
}
