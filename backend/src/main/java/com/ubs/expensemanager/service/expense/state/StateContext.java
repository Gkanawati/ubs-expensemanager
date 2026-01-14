package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.messages.Messages;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.ExpenseRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Context object passed to state operations, providing access to expense data, current user, and
 * necessary dependencies.
 *
 * <p>This class encapsulates the context needed for state transitions,
 * including the expense being processed, the user performing the action, and the repository for
 * persistence.</p>
 *
 * <p>It also provides helper methods for common validation and persistence
 * operations used by multiple states.</p>
 */
@Getter
@Builder
@Slf4j
public class StateContext {

  private final Expense expense;
  private final User currentUser;
  private final ExpenseRepository expenseRepository;

  /**
   * Updates the expense status and logs the change.
   *
   * @param newStatus the new status to set
   */
  public void setExpenseStatus(ExpenseStatus newStatus) {
    log.info(Messages.formatMessage(Messages.TRANSITIONING_EXPENSE,
        expense.getId(), expense.getStatus(), newStatus));
    expense.setStatus(newStatus);
  }

  /**
   * Persists the expense entity to the database.
   */
  public void saveExpense() {
    expenseRepository.save(expense);
  }

  /**
   * Validates that the current user is in the same department as the expense owner. Used by manager
   * approval/rejection to enforce department constraints.
   *
   * @throws UnauthorizedExpenseAccessException if departments don't match
   */
  public void validateSameDepartment() {
    if (!expense.getUser().getDepartment().getId()
        .equals(currentUser.getDepartment().getId())) {
      throw new UnauthorizedExpenseAccessException("You can only process expenses for employees in your department");
    }
  }

  /**
   * Checks if the current user has the specified role.
   *
   * @param role the role to check
   * @return true if the user has the role, false otherwise
   */
  public boolean hasRole(UserRole role) {
    return currentUser.getRole() == role;
  }

  /**
   * Checks if the current user does not have the specified role.
   *
   * @param role the role to check
   * @return true if the user does NOT have the role, false otherwise
   */
  public boolean doesNotHaveRole(UserRole role) {
    return !hasRole(role);
  }
}
