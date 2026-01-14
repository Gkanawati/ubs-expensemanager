package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.messages.Messages;
import com.ubs.expensemanager.model.ExpenseStatus;
import java.util.Collections;
import java.util.Set;
import lombok.Getter;

/**
 * Base implementation providing common functionality for all expense states. Implements template
 * method pattern for transition validation.
 *
 * <p>This abstract class handles:
 * <ul>
 *   <li>Storage of the state's status and valid transitions</li>
 *   <li>Transition validation logic</li>
 *   <li>Query methods for state information</li>
 * </ul>
 *
 * <p>Subclasses must implement the approve() and reject() methods to provide
 * state-specific behavior.</p>
 */
@Getter
public abstract class AbstractExpenseState implements ExpenseState {

  private final ExpenseStatus status;
  private final Set<ExpenseStatus> validTransitions;

  /**
   * Constructs a new abstract expense state.
   *
   * @param status           the expense status this state represents
   * @param validTransitions the set of statuses this state can transition to (will be made
   *                         immutable)
   */
  protected AbstractExpenseState(ExpenseStatus status, Set<ExpenseStatus> validTransitions) {
    this.status = status;
    this.validTransitions = Collections.unmodifiableSet(validTransitions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canTransitionTo(ExpenseStatus newStatus) {
    return validTransitions.contains(newStatus);
  }

  /**
   * Validates that the target status is a valid transition from this state. Throws an exception if
   * the transition is invalid.
   *
   * @param targetStatus the status to transition to
   * @throws InvalidStatusTransitionException if the transition is not allowed
   */
  protected void validateTransition(ExpenseStatus targetStatus) {
    if (!canTransitionTo(targetStatus)) {
      throw new InvalidStatusTransitionException(
          Messages.formatMessage(Messages.CANNOT_TRANSITION_FROM_TO, status, targetStatus)
      );
    }
  }
}
