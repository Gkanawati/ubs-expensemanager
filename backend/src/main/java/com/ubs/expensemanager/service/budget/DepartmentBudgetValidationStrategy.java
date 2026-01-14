package com.ubs.expensemanager.service.budget;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
import com.ubs.expensemanager.exception.BudgetExceededException;
import com.ubs.expensemanager.model.Department;
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
 * Strategy implementation for validating department budget limits.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentBudgetValidationStrategy implements BudgetValidationStrategy {

    private final ExpenseRepository expenseRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void validate(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        Department department = expense.getUser().getDepartment();
        if (department != null) {
            // Only validate daily budget if the department has a daily budget limit
            if (department.getDailyBudget() != null) {
                validateDailyBudget(userId, category, expense, newAmount, department);
            }
            validateMonthlyBudget(expense, newAmount, department);
        }
    }

    /**
     * Validates daily budget limits for the department.
     * All amounts are converted to USD before comparison.
     */
    private void validateDailyBudget(Long userId, ExpenseCategory category, Expense expense, 
                                    BigDecimal newAmount, Department department) {
        // Convert the new expense amount to USD
        BigDecimal newAmountUsd = newAmount.divide(expense.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        // Convert the department budget limit to USD
        BigDecimal dailyBudgetUsd = department.getDailyBudget().divide(department.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);

        BigDecimal deptDailyTotal;
        if (expense.getId() == null) {
            deptDailyTotal = Optional.ofNullable(
                    expenseRepository.sumAmountByDepartmentAndDate(department.getId(), expense.getExpenseDate())
            ).orElse(BigDecimal.ZERO);
        } else {
            deptDailyTotal = Optional.ofNullable(
                    expenseRepository.sumAmountByDepartmentAndDateExcludingExpense(department.getId(), expense.getExpenseDate(), expense.getId())
            ).orElse(BigDecimal.ZERO);
        }

        BigDecimal newDeptDailyTotal = deptDailyTotal.add(newAmountUsd);

        if (department.getDailyBudget() != null && newDeptDailyTotal.compareTo(dailyBudgetUsd) > 0) {
            log.warn("Daily department budget exceeded for department {} on {}: current={}, new={}, limit={} (all in USD)",
                    department.getName(), expense.getExpenseDate(), deptDailyTotal, newDeptDailyTotal, dailyBudgetUsd);

            // Publish domain event for daily department budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.DEPARTAMENT)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(deptDailyTotal)
                    .newTotal(newDeptDailyTotal)
                    .budgetLimit(dailyBudgetUsd)
                    .date(expense.getExpenseDate())
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }

    /**
     * Validates monthly budget limits for the department.
     * All amounts are converted to USD before comparison.
     */
    private void validateMonthlyBudget(Expense expense, BigDecimal newAmount, Department department) {
        // Convert the new expense amount to USD
        BigDecimal newAmountUsd = newAmount.divide(expense.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        // Convert the department budget limit to USD
        BigDecimal monthlyBudgetUsd = department.getMonthlyBudget().divide(department.getCurrency().getExchangeRate(), 2, RoundingMode.HALF_UP);
        
        YearMonth yearMonth = YearMonth.from(expense.getExpenseDate());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        BigDecimal deptMonthlyTotal;
        if (expense.getId() == null) {
            deptMonthlyTotal = Optional.ofNullable(
                    expenseRepository.sumAmountByDepartmentAndDateRange(department.getId(), monthStart, monthEnd)
            ).orElse(BigDecimal.ZERO);
        } else {
            deptMonthlyTotal = Optional.ofNullable(
                    expenseRepository.sumAmountByDepartmentAndDateRangeExcludingExpense(department.getId(), monthStart, monthEnd, expense.getId())
            ).orElse(BigDecimal.ZERO);
        }

        BigDecimal newDeptMonthlyTotal = deptMonthlyTotal.add(newAmountUsd);

        if (newDeptMonthlyTotal.compareTo(monthlyBudgetUsd) > 0) {
            log.warn("Monthly department budget exceeded for department {} in {}: current={}, new={}, limit={} (all in USD)",
                    department.getName(), yearMonth, deptMonthlyTotal, newDeptMonthlyTotal, monthlyBudgetUsd);

            throw new BudgetExceededException(department.getName(), yearMonth, deptMonthlyTotal,
                newDeptMonthlyTotal, monthlyBudgetUsd);
        }
    }
}
