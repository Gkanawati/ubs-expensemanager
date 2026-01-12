package com.ubs.expensemanager.service.budget;

import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;

import java.math.BigDecimal;

/**
 * Strategy interface for validating different types of budget limits.
 */
public interface BudgetValidationStrategy {
    
    /**
     * Validates budget limits for the expense.
     * Logs warnings if budget is exceeded but does not block creation.
     *
     * @param userId user ID
     * @param category expense category
     * @param expense object expense
     * @param newAmount amount of the new expense
     */
    void validate(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount);
}