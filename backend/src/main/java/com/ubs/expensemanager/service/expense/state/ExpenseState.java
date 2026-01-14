package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.model.ExpenseStatus;
import java.util.Set;

/**
 * Defines the contract for expense state behavior in the approval workflow. Each concrete state
 * encapsulates the transition rules and authorization logic specific to that status.
 *
 * <p>This interface is part of the State pattern implementation where each
 * expense status (PENDING, APPROVED_BY_MANAGER, APPROVED_BY_FINANCE, REJECTED) has a corresponding
 * state implementation that knows:
 * <ul>
 *   <li>What transitions are valid from that state</li>
 *   <li>Who is authorized to perform those transitions</li>
 *   <li>What business rules apply (e.g., department validation for managers)</li>
 * </ul>
 */
public interface ExpenseState {

  /**
   * Approves the expense, transitioning it to the next state in the approval workflow.
   *
   * @param context contains expense, user, and repository access
   * @throws InvalidStatusTransitionException   if transition is not allowed
   * @throws UnauthorizedExpenseAccessException if user lacks permission
   */
  void approve(StateContext context);

  /**
   * Rejects the expense, transitioning it to the REJECTED state.
   *
   * @param context contains expense, user, and repository access
   * @throws InvalidStatusTransitionException   if transition is not allowed
   * @throws UnauthorizedExpenseAccessException if user lacks permission
   */
  void reject(StateContext context);

  /**
   * Checks if transition to the new status is valid from the current state.
   *
   * @param newStatus the target status
   * @return true if the transition is valid, false otherwise
   */
  boolean canTransitionTo(ExpenseStatus newStatus);

  /**
   * Returns the expense status this state represents.
   *
   * @return the expense status
   */
  ExpenseStatus getStatus();

  /**
   * Returns all valid outgoing transitions from this state.
   *
   * @return set of allowed target statuses (immutable)
   */
  Set<ExpenseStatus> getValidTransitions();
}
