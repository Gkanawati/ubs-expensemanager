package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.messages.Messages;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.UserRole;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State for expenses that have been approved by a manager and are awaiting finance approval.
 *
 * <p>From APPROVED_BY_MANAGER state, a finance user can:
 * <ul>
 *   <li>Approve → transitions to APPROVED_BY_FINANCE (terminal)</li>
 *   <li>Reject → transitions to REJECTED (terminal)</li>
 * </ul>
 *
 * <p>Authorization requirements:
 * <ul>
 *   <li>User must have FINANCE role</li>
 *   <li>No department restriction (finance can process expenses from any department)</li>
 * </ul>
 */
@Component
@Slf4j
public class ApprovedByManagerState extends AbstractExpenseState {

  /**
   * Constructs the APPROVED_BY_MANAGER state with valid transitions.
   */
  public ApprovedByManagerState() {
    super(
        ExpenseStatus.APPROVED_BY_MANAGER,
        Set.of(ExpenseStatus.APPROVED_BY_FINANCE, ExpenseStatus.REJECTED)
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Expense approve(StateContext context) {
    log.debug(Messages.formatMessage(Messages.PROCESSING_FINANCE_APPROVAL,
        context.getExpense().getId()));

    if (context.doesNotHaveRole(UserRole.FINANCE)) {
      throw new UnauthorizedExpenseAccessException(
          "Only finance users can approve expenses at this stage"
      );
    }

    validateTransition(ExpenseStatus.APPROVED_BY_FINANCE);
    context.setExpenseStatus(ExpenseStatus.APPROVED_BY_FINANCE);
    context.saveExpense();

    log.info(Messages.formatMessage(Messages.EXPENSE_APPROVED_BY_FINANCE,
        context.getExpense().getId(), context.getCurrentUser().getUsername()));

    return context.getExpense();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Expense reject(StateContext context) {
    log.debug(Messages.formatMessage(Messages.PROCESSING_FINANCE_REJECTION,
        context.getExpense().getId()));

    if (context.doesNotHaveRole(UserRole.FINANCE)) {
      throw new UnauthorizedExpenseAccessException(
          "Only finance users can reject expenses at this stage"
      );
    }

    validateTransition(ExpenseStatus.REJECTED);
    context.setExpenseStatus(ExpenseStatus.REJECTED);
    context.saveExpense();

    log.info(Messages.formatMessage(Messages.EXPENSE_REJECTED_BY_FINANCE,
        context.getExpense().getId(), context.getCurrentUser().getUsername()));

    return context.getExpense();
  }
}
