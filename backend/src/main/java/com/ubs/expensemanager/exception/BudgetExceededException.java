package com.ubs.expensemanager.exception;

import com.ubs.expensemanager.messages.Messages;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Getter;

/**
 * Exception thrown when an expense would exceed the monthly department budget limit. This is a
 * blocking exception that prevents expense creation.
 */
@Getter
public class BudgetExceededException extends RuntimeException {

  private final String departmentName;
  private final YearMonth yearMonth;
  private final BigDecimal currentTotal;
  private final BigDecimal newTotal;
  private final BigDecimal budgetLimit;

  public BudgetExceededException(String departmentName, YearMonth yearMonth,
      BigDecimal currentTotal, BigDecimal newTotal, BigDecimal budgetLimit) {

    super(Messages.formatMessage(Messages.MONTHLY_DEPARTMENT_BUDGET_EXCEEDED, departmentName,
        yearMonth, currentTotal, newTotal, budgetLimit));

    this.departmentName = departmentName;
    this.yearMonth = yearMonth;
    this.currentTotal = currentTotal;
    this.newTotal = newTotal;
    this.budgetLimit = budgetLimit;
  }
}
