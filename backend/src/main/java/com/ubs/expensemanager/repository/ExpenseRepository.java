package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository responsible for data access operations related to {@link Expense}.
 *
 * <p>This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA, as well as custom queries
 * for budget validation and ownership checks.</p>
 */
@Repository
public interface ExpenseRepository extends
        JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    /**
     * Calculates the total expense amount for a user in a specific category on a specific date.
     * Excludes REJECTED expenses from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param date the expense date
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate = :date " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date
    );

    /**
     * Calculates the total expense amount for a user in a specific category on a specific date,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param categoryId the expense category ID
     * @param date the expense date
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate = :date " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByCategoryAndDateExcludingExpense(
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date,
            @Param("expenseId") Long expenseId
    );

    /**
     * Calculates the total expense amount for a user in a specific category within a date range.
     * Excludes REJECTED expenses from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculates the total expense amount for a user in a specific category within a date range,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param categoryId the expense category ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByCategoryAndDateRangeExcludingExpense(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseId") Long expenseId
    );

    /**
     * Calculates the total expense amount for a department on a specific date.
     * Excludes REJECTED expenses from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param departmentId the department ID
     * @param date the expense date
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate = :date " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDate(
            @Param("departmentId") Long departmentId,
            @Param("date") LocalDate date
    );

    /**
     * Calculates the total expense amount for a department on a specific date,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param departmentId the department ID
     * @param date the expense date
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate = :date " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateExcludingExpense(
            @Param("departmentId") Long departmentId,
            @Param("date") LocalDate date,
            @Param("expenseId") Long expenseId
    );

    /**
     * Calculates the total expense amount for a department within a date range.
     * Excludes REJECTED expenses from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param departmentId the department ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculates the total expense amount for a department within a date range,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     * All amounts are converted to USD before summing.
     *
     * @param departmentId the department ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts in USD, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(ROUND(SUM(e.amount / e.currency.exchangeRate), 2), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateRangeExcludingExpense(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseId") Long expenseId
    );

    /**
     * Finds all expenses within a date range excluding REJECTED expenses.
     * Used for generating reports.
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return list of expenses within the date range
     */
    List<Expense> findAllByExpenseDateBetweenAndStatusNot(
            LocalDate startDate,
            LocalDate endDate,
            ExpenseStatus status
    );

    /**
     * Finds all expenses for a specific user excluding REJECTED expenses.
     * Used for personal expense reports.
     *
     * @param userId the user ID
     * @return list of user's expenses
     */
    List<Expense> findAllByUserIdAndStatusNot(Long userId, ExpenseStatus status);

    /**
     * Finds the most recent expenses for a specific user, excluding REJECTED expenses.
     * Orders by expense date descending and then by ID descending.
     *
     * @param userId the user ID
     * @param status the status to exclude
     * @param limit the maximum number of results
     * @return list of recent expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.status != :status " +
           "ORDER BY e.expenseDate DESC, e.id DESC")
    List<Expense> findTopByUserIdAndStatusNotOrderByExpenseDateDesc(
            @Param("userId") Long userId,
            @Param("status") ExpenseStatus status,
            @Param("limit") org.springframework.data.domain.Pageable pageable
    );

    /**
     * Finds all expenses for a specific user within a date range, excluding REJECTED expenses.
     *
     * @param userId the user ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param status the status to exclude
     * @return list of user's expenses within the date range
     */
    List<Expense> findAllByUserIdAndExpenseDateBetweenAndStatusNot(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            ExpenseStatus status
    );

    /**
     * Finds all expenses for a specific user with a specific status.
     *
     * @param userId the user ID
     * @param status the expense status
     * @return list of user's expenses with the specified status
     */
    List<Expense> findAllByUserIdAndStatus(Long userId, ExpenseStatus status);

    /**
     * Finds all expenses excluding a specific status.
     * Used for generating overall reports.
     *
     * @param status the status to exclude
     * @return list of all expenses excluding the specified status
     */
    List<Expense> findAllByStatusNot(ExpenseStatus status);

    /**
     * Finds the most recent expenses, excluding a specific status.
     * Orders by expense date descending and then by ID descending.
     *
     * @param status the status to exclude
     * @param pageable the pagination information
     * @return list of recent expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.status != :status " +
           "ORDER BY e.expenseDate DESC, e.id DESC")
    List<Expense> findTopByStatusNotOrderByExpenseDateDesc(
            @Param("status") ExpenseStatus status,
            org.springframework.data.domain.Pageable pageable
    );
}
