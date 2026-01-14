package com.ubs.expensemanager.service.budget;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

/**
 * Strategy implementation for validating category budget limits.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryBudgetValidationStrategy implements BudgetValidationStrategy {

    private final ExpenseRepository expenseRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void validate(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        validateDailyBudget(userId, category, expense, newAmount);
        validateMonthlyBudget(userId, category, expense, newAmount);
    }

    /**
     * Validates daily budget limits for the category.
     * All amounts are converted to USD before comparison.
     */
    private void validateDailyBudget(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        // Convert the new expense amount to USD
        BigDecimal newAmountUsd = newAmount.divide(expense.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        // Convert the category budget limit to USD
        BigDecimal dailyBudgetUsd = category.getDailyBudget().divide(category.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        // The repository already returns amounts in USD
        BigDecimal dailyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByCategoryAndDateExcludingExpense(category.getId(), expense.getExpenseDate(), expense.getId())
        ).orElse(BigDecimal.ZERO);
        BigDecimal newDailyTotal = dailyTotal.add(newAmountUsd);

        if (newDailyTotal.compareTo(dailyBudgetUsd) > 0) {
            log.warn("Daily budget exceeded for user {} in category {} on {}: current={}, new={}, limit={} (all in USD)",
                    userId, category.getName(), expense.getExpenseDate(), dailyTotal, newDailyTotal, dailyBudgetUsd);

            // Publish domain event for daily budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(dailyTotal)
                    .newTotal(newDailyTotal)
                    .budgetLimit(dailyBudgetUsd)
                    .date(expense.getExpenseDate())
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }

    /**
     * Validates monthly budget limits for the category.
     * All amounts are converted to USD before comparison.
     */
    private void validateMonthlyBudget(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        // Convert the new expense amount to USD
        BigDecimal newAmountUsd = newAmount.divide(expense.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        // Convert the category budget limit to USD
        BigDecimal monthlyBudgetUsd = category.getMonthlyBudget().divide(category.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        YearMonth yearMonth = YearMonth.from(expense.getExpenseDate());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // The repository already returns amounts in USD
        BigDecimal monthlyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByCategoryAndDateRangeExcludingExpense(category.getId(), monthStart, monthEnd, expense.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal newMonthlyTotal = monthlyTotal.add(newAmountUsd);

        if (newMonthlyTotal.compareTo(monthlyBudgetUsd) > 0) {
            log.warn("Monthly budget exceeded for user {} in category {} in {}: current={}, new={}, limit={} (all in USD)",
                    userId, category.getName(), yearMonth, monthlyTotal, newMonthlyTotal, monthlyBudgetUsd);

            // Publish domain event for monthly budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(monthlyTotal)
                    .newTotal(newMonthlyTotal)
                    .budgetLimit(monthlyBudgetUsd)
                    .yearMonth(yearMonth)
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }
}
