package com.ubs.expensemanager.event;

import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Event that is published when a budget is exceeded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExceededEvent {

    /**
     * Type of budget that was exceeded.
     */
    public enum BudgetType {
        DEPARTAMENT,
        CATEGORY,
        ALL
    }

    /**
     * Type of budget that was exceeded (daily or monthly).
     */
    private BudgetType budgetType;

    /**
     * The expense that caused the budget to be exceeded.
     */
    private Expense expense;

    /**
     * The category whose budget was exceeded.
     */
    private ExpenseCategory category;

    /**
     * ID of the user who created the expense.
     */
    private Long userId;

    /**
     * Total amount spent in this category before the new expense.
     */
    private BigDecimal currentTotal;

    /**
     * Total amount spent in this category after the new expense.
     */
    private BigDecimal newTotal;

    /**
     * The budget limit that was exceeded.
     */
    private BigDecimal budgetLimit;

    /**
     * The date for which the daily budget was exceeded.
     * Only set when budgetType is DAILY.
     */
    private LocalDate date;

    /**
     * The year and month for which the monthly budget was exceeded.
     * Only set when budgetType is MONTHLY.
     */
    private YearMonth yearMonth;
}
