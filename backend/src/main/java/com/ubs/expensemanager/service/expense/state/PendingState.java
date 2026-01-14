package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.UserRole;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State for expenses that are pending initial manager approval.
 *
 * <p>From PENDING state, a manager from the same department can:
 * <ul>
 *   <li>Approve → transitions to APPROVED_BY_MANAGER</li>
 *   <li>Reject → transitions to REJECTED</li>
 * </ul>
 *
 * <p>Authorization requirements:
 * <ul>
 *   <li>User must have MANAGER role</li>
 *   <li>Manager must be in the same department as the expense owner</li>
 * </ul>
 */
@Component
@Slf4j
public class PendingState extends AbstractExpenseState {

  /**
   * Constructs the PENDING state with valid transitions.
   */
  public PendingState() {
    super(
        ExpenseStatus.PENDING,
        Set.of(ExpenseStatus.APPROVED_BY_MANAGER, ExpenseStatus.REJECTED)
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void approve(StateContext context) {
    log.debug("Processing approval for PENDING expense {}", context.getExpense().getId());

    // Validate authorization
    if (context.doesNotHaveRole(UserRole.MANAGER)) {
      throw new UnauthorizedExpenseAccessException(
          "Only managers can approve pending expenses"
      );
    }

    context.validateSameDepartment();

    // Validate and perform transition
    validateTransition(ExpenseStatus.APPROVED_BY_MANAGER);
    context.setExpenseStatus(ExpenseStatus.APPROVED_BY_MANAGER);
    context.saveExpense();

    log.info("Expense {} approved by manager {}",
        context.getExpense().getId(), context.getCurrentUser().getUsername());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reject(StateContext context) {
    log.debug("Processing rejection for PENDING expense {}", context.getExpense().getId());

    // Validate authorization
    if (context.doesNotHaveRole(UserRole.MANAGER)) {
      throw new UnauthorizedExpenseAccessException(
          "Only managers can reject pending expenses"
      );
    }

    context.validateSameDepartment();

    // Validate and perform transition
    validateTransition(ExpenseStatus.REJECTED);
    context.setExpenseStatus(ExpenseStatus.REJECTED);
    context.saveExpense();

    log.info("Expense {} rejected by manager {}",
        context.getExpense().getId(), context.getCurrentUser().getUsername());
  }
}
