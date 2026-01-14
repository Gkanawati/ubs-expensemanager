package com.ubs.expensemanager.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized logging messages for expense state transitions. This class provides consistent log
 * messages used across all state implementations.
 *
 * <p>Using constants ensures consistency, makes message updates easier, and
 * improves maintainability.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Messages {

  public static final String USER_ATTEMPTING_ACTION = "User {} (role: {}) attempting to {} expense {} with status {}";

  public static final String PROCESSING_PENDING_APPROVAL = "Processing approval for PENDING expense {}";
  public static final String PROCESSING_PENDING_REJECTION = "Processing rejection for PENDING expense {}";
  public static final String PROCESSING_FINANCE_APPROVAL = "Processing finance approval for expense {}";
  public static final String PROCESSING_FINANCE_REJECTION = "Processing finance rejection for expense {}";

  public static final String EXPENSE_APPROVED_BY_MANAGER = "Expense {} approved by manager {}";
  public static final String EXPENSE_REJECTED_BY_MANAGER = "Expense {} rejected by manager {}";
  public static final String EXPENSE_APPROVED_BY_FINANCE = "Expense {} approved by finance user {}";
  public static final String EXPENSE_REJECTED_BY_FINANCE = "Expense {} rejected by finance user {}";
  public static final String TRANSITIONING_EXPENSE = "Transitioning expense {} from {} to {}";

  /**
   * Format message
   *
   * @param template template message
   * @param params   message parameters
   * @return formatted message
   */
  public static String formatMessage(String template, Object... params) {
    return String.format(template, params);
  }

}
