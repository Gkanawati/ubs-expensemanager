package com.ubs.expensemanager.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized messages for the application. This class provides consistent messages for logging and
 * exceptions across all components.
 *
 * <p>Using constants ensures consistency, makes message updates easier, and improves
 * maintainability. Templates use String.format placeholders (%s, %d, %f, etc.) for type-safe
 * formatting.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Messages {

  // ===== Expense State Transition Messages =====
  public static final String USER_ATTEMPTING_ACTION = "User %s (role: %s) attempting to %s expense %s with status %s";
  public static final String PROCESSING_PENDING_APPROVAL = "Processing approval for PENDING expense %s";
  public static final String PROCESSING_PENDING_REJECTION = "Processing rejection for PENDING expense %s";
  public static final String PROCESSING_FINANCE_APPROVAL = "Processing finance approval for expense %s";
  public static final String PROCESSING_FINANCE_REJECTION = "Processing finance rejection for expense %s";
  public static final String EXPENSE_APPROVED_BY_MANAGER = "Expense %s approved by manager %s";
  public static final String EXPENSE_REJECTED_BY_MANAGER = "Expense %s rejected by manager %s";
  public static final String EXPENSE_APPROVED_BY_FINANCE = "Expense %s approved by finance user %s";
  public static final String EXPENSE_REJECTED_BY_FINANCE = "Expense %s rejected by finance user %s";
  public static final String TRANSITIONING_EXPENSE = "Transitioning expense %s from %s to %s";

  // ===== Exception Messages =====

  // ===== Resource Not Found =====
  public static final String EXPENSE_NOT_FOUND = "Expense not found";
  public static final String EXPENSE_CATEGORY_NOT_FOUND = "Expense category not found";
  public static final String EXPENSE_CATEGORY_NOT_FOUND_IN_AUDIT = "Expense category not found in audit history";
  public static final String CURRENCY_NOT_FOUND = "Currency not found: %s";
  public static final String CURRENCY_NOT_FOUND_WITH_ID = "Currency not found with ID: %s";
  public static final String DEPARTMENT_NOT_FOUND = "Department not found";
  public static final String USER_NOT_FOUND = "User not found";
  public static final String USER_NOT_FOUND_WITH_ID = "There is no user with id %s";
  public static final String ALERT_NOT_FOUND = "Alert not found";
  public static final String NO_AUDIT_RECORD_FOUND = "No audit record found for category at specified date";

  // ===== Conflict =====
  public static final String DEPARTMENT_NAME_CONFLICT = "Department with this name already exists";
  public static final String EXPENSE_CATEGORY_NAME_CONFLICT = "Expense category with this name already exists";

  // ===== Unauthorized Access =====
  public static final String UNAUTHORIZED_ACCESS_EXPENSE = "You do not have permission to access this expense";

  // ===== Invalid Status Transition =====
  public static final String CANNOT_UPDATE_EXPENSE_STATUS = "Cannot update expense with status %s. Only PENDING expenses can be updated.";
  public static final String CANNOT_DELETE_EXPENSE_STATUS = "Cannot delete expense with status %s. Only PENDING expenses can be deleted.";
  public static final String CANNOT_TRANSITION_FROM_TO = "Cannot transition from %s to %s";
  public static final String CANNOT_APPROVE_TERMINAL_STATUS = "Cannot approve expense with terminal status %s";
  public static final String CANNOT_REJECT_TERMINAL_STATUS = "Cannot reject expense with terminal status %s";
  public static final String NO_STATE_FOUND_FOR_STATUS = "No state found for status: %s";

  // ===== Validation Errors =====
  public static final String USER_EMAIL_EXISTS = "The email '%s' is already registered";
  public static final String EMAIL_CANNOT_BE_CHANGED = "Email cannot be changed";
  public static final String ROLE_CANNOT_BE_CHANGED = "Role cannot be changed";

  /**
   * Formats a message template with parameters using String.format.
   *
   * @param template template message with String.format placeholders (%s, %d, %f, etc.)
   * @param params   message parameters
   * @return formatted message
   */
  public static String formatMessage(String template, Object... params) {
    return String.format(template, params);
  }
}
