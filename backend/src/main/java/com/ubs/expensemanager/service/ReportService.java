package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.DepartmentExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.LastExpenseDto;
import com.ubs.expensemanager.dto.response.PersonalExpenseSummaryResponse;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.DepartmentRepository;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.util.CurrencyConverter;
import com.ubs.expensemanager.util.DateRangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating expense reports.
 * 
 * <p>This service handles data aggregation for various types of expense reports.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Generates expense report grouped by employee for a given date range.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return list of employee expense totals in USD
     */
    @Transactional(readOnly = true)
    public List<EmployeeExpenseReportResponse> getExpensesByEmployeeReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateRangeValidator.validate(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating expense report by employee from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<EmployeeExpenseReportResponse> report = getExpensesByEmployee(effectiveStartDate, effectiveEndDate);
        
        log.info("Report generated with {} employees", report.size());
        return report;
    }

    /**
     * Generates CSV report grouped by employee for a given date range.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return CSV formatted string
     */
    @Transactional(readOnly = true)
    public String getExpensesByEmployeeCsvReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateRangeValidator.validate(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating CSV expense report by employee from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<EmployeeExpenseReportResponse> report = getExpensesByEmployee(effectiveStartDate, effectiveEndDate);
        String csv = generateEmployeeCsv(report);
        
        log.info("CSV report generated with {} employees", report.size());
        return csv;
    }

    /**
     * Generates filename for CSV download.
     * 
     * @param baseName base name for the file (e.g., "expenses-by-employee", "expenses-by-category")
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return filename
     */
    public String generateCsvFilename(String baseName, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s_%s_to_%s.csv", 
                baseName,
                effectiveStartDate.format(formatter), 
                effectiveEndDate.format(formatter));
    }

    /**
     * Generates expense report grouped by employee for a given date range.
     * All amounts are converted to USD for comparison.
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of employee expense totals in USD
     */
    @Transactional(readOnly = true)
    public List<EmployeeExpenseReportResponse> getExpensesByEmployee(LocalDate startDate, LocalDate endDate) {
        // Fetch all expenses within the date range (excluding REJECTED)
        List<Expense> expenses = expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                startDate, 
                endDate,
                ExpenseStatus.REJECTED
        );
        
        // Group by employee and sum amounts (converted to USD)
        Map<String, BigDecimal> employeeTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getUser().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                CurrencyConverter::convertToUsd,
                                BigDecimal::add
                        )
                ));
        
        // Convert map to list of DTOs and sort by total (descending)
        List<EmployeeExpenseReportResponse> report = employeeTotals.entrySet().stream()
                .map(entry -> EmployeeExpenseReportResponse.builder()
                        .employee(entry.getKey())
                        .total(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                .collect(Collectors.toList());
        
        return report;
    }

    /**
     * Generates CSV content from employee report data.
     * 
     * @param report the report data
     * @return CSV formatted string
     */
    private String generateEmployeeCsv(List<EmployeeExpenseReportResponse> report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee,Total (USD)\n");
        
        for (EmployeeExpenseReportResponse row : report) {
            csv.append(escapeCsv(row.getEmployee()))
               .append(",")
               .append(row.getTotal())
               .append("\n");
        }
        
        return csv.toString();
    }

    /**
     * Generates expense report grouped by category for a given date range.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return list of category expense totals in USD
     */
    @Transactional(readOnly = true)
    public List<CategoryExpenseReportResponse> getExpensesByCategoryReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateRangeValidator.validate(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating expense report by category from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<CategoryExpenseReportResponse> report = getExpensesByCategory(effectiveStartDate, effectiveEndDate);
        
        log.info("Report generated with {} categories", report.size());
        return report;
    }

    /**
     * Generates expense report grouped by category for a given date range.
     * All amounts are converted to USD for comparison.
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of category expense totals in USD
     */
    private List<CategoryExpenseReportResponse> getExpensesByCategory(LocalDate startDate, LocalDate endDate) {
        // Fetch all expenses within the date range (excluding REJECTED)
        List<Expense> expenses = expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                startDate, 
                endDate,
                ExpenseStatus.REJECTED
        );
        
        // Group by category and sum amounts (converted to USD)
        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getExpenseCategory().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                CurrencyConverter::convertToUsd,
                                BigDecimal::add
                        )
                ));
        
        // Convert map to list of DTOs and sort by total (descending)
        List<CategoryExpenseReportResponse> report = categoryTotals.entrySet().stream()
                .map(entry -> CategoryExpenseReportResponse.builder()
                        .category(entry.getKey())
                        .total(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                .collect(Collectors.toList());
        
        return report;
    }

    /**
     * Generates CSV report grouped by category for a given date range.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return CSV formatted string
     */
    @Transactional(readOnly = true)
    public String getExpensesByCategoryCsvReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateRangeValidator.validate(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating CSV expense report by category from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<CategoryExpenseReportResponse> report = getExpensesByCategory(effectiveStartDate, effectiveEndDate);
        String csv = generateCategoryCsv(report);
        
        log.info("CSV report generated with {} categories", report.size());
        return csv;
    }

    /**
     * Generates CSV content from category report data.
     * 
     * @param report the report data
     * @return CSV formatted string
     */
    private String generateCategoryCsv(List<CategoryExpenseReportResponse> report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Category,Total (USD)\n");
        
        for (CategoryExpenseReportResponse row : report) {
            csv.append(escapeCsv(row.getCategory()))
               .append(",")
               .append(row.getTotal())
               .append("\n");
        }
        
        return csv.toString();
    }

    /**
     * Generates expense report grouped by department with budget tracking.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to current date)
     * @param endDate end date (nullable, defaults to current date)
     * @return list of department expense reports with budget information in USD
     */
    @Transactional(readOnly = true)
    public List<DepartmentExpenseReportResponse> getExpensesByDepartmentReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        // Special validation for department reports: same month/year or single day
        DateRangeValidator.validateSameMonthOrSingleDay(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating expense report by department from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<DepartmentExpenseReportResponse> report;
        
        // Check if it's a daily report or period report
        if (effectiveStartDate.equals(effectiveEndDate)) {
            report = getExpensesByDepartmentDaily(effectiveStartDate);
        } else {
            report = getExpensesByDepartmentPeriod(effectiveStartDate, effectiveEndDate);
        }
        
        log.info("Report generated with {} departments", report.size());
        return report;
    }

    /**
     * Generates CSV report grouped by department with budget tracking.
     * Applies defaults and validates dates.
     * 
     * @param startDate start date (nullable, defaults to current date)
     * @param endDate end date (nullable, defaults to current date)
     * @return CSV formatted string
     */
    @Transactional(readOnly = true)
    public String getExpensesByDepartmentCsvReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        // Special validation for department reports: same month/year or single day
        DateRangeValidator.validateSameMonthOrSingleDay(effectiveStartDate, effectiveEndDate);
        
        log.info("Generating CSV expense report by department from {} to {}", effectiveStartDate, effectiveEndDate);
        
        List<DepartmentExpenseReportResponse> report;
        
        // Check if it's a daily report or period report
        if (effectiveStartDate.equals(effectiveEndDate)) {
            report = getExpensesByDepartmentDaily(effectiveStartDate);
        } else {
            report = getExpensesByDepartmentPeriod(effectiveStartDate, effectiveEndDate);
        }
        
        String csv = generateDepartmentCsv(report);
        
        log.info("CSV report generated with {} departments", report.size());
        return csv;
    }

    /**
     * Generates expense report grouped by department for a period within the same month.
     * Uses monthly budget for comparison.
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of department expense reports with budget information
     */
    private List<DepartmentExpenseReportResponse> getExpensesByDepartmentPeriod(LocalDate startDate, LocalDate endDate) {
        // Fetch all expenses within the date range (excluding REJECTED)
        List<Expense> expenses = expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                startDate, 
                endDate,
                ExpenseStatus.REJECTED
        );
        
        // Group by department and sum amounts (converted to USD)
        Map<Department, BigDecimal> departmentTotals = expenses.stream()
                .filter(expense -> expense.getUser().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        expense -> expense.getUser().getDepartment(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                CurrencyConverter::convertToUsd,
                                BigDecimal::add
                        )
                ));
        
        // Get all departments to include those with no expenses
        List<Department> allDepartments = departmentRepository.findAll();
        
        // Convert map to list of DTOs with budget calculations (using monthly budget)
        List<DepartmentExpenseReportResponse> report = allDepartments.stream()
                .map(department -> {
                    BigDecimal used = departmentTotals.getOrDefault(department, BigDecimal.ZERO)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    // Convert monthly budget to USD using department's currency exchange rate
                    BigDecimal exchangeRate = department.getCurrency().getExchangeRate();
                    BigDecimal budgetInUsd = department.getMonthlyBudget()
                            .divide(exchangeRate, 2, RoundingMode.HALF_UP);
                    
                    BigDecimal remaining;
                    BigDecimal overBudget;
                    
                    if (used.compareTo(budgetInUsd) <= 0) {
                        // Within budget
                        remaining = budgetInUsd.subtract(used).setScale(2, RoundingMode.HALF_UP);
                        overBudget = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    } else {
                        // Over budget
                        remaining = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                        overBudget = used.subtract(budgetInUsd).setScale(2, RoundingMode.HALF_UP);
                    }
                    
                    return DepartmentExpenseReportResponse.builder()
                            .department(department.getName())
                            .used(used)
                            .remaining(remaining)
                            .overBudget(overBudget)
                            .build();
                })
                .sorted((a, b) -> b.getUsed().compareTo(a.getUsed()))
                .collect(Collectors.toList());
        
        return report;
    }

    /**
     * Generates expense report grouped by department for a single day.
     * Uses daily budget for comparison.
     * 
     * @param date the date to report on
     * @return list of department expense reports with budget information
     */
    private List<DepartmentExpenseReportResponse> getExpensesByDepartmentDaily(LocalDate date) {
        // Fetch all expenses for the specific date (excluding REJECTED)
        List<Expense> expenses = expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                date, 
                date,
                ExpenseStatus.REJECTED
        );
        
        // Group by department and sum amounts (converted to USD)
        Map<Department, BigDecimal> departmentTotals = expenses.stream()
                .filter(expense -> expense.getUser().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        expense -> expense.getUser().getDepartment(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                CurrencyConverter::convertToUsd,
                                BigDecimal::add
                        )
                ));
        
        // Get all departments to include those with no expenses
        List<Department> allDepartments = departmentRepository.findAll();
        
        // Convert map to list of DTOs with budget calculations (using daily budget)
        List<DepartmentExpenseReportResponse> report = allDepartments.stream()
                .map(department -> {
                    BigDecimal used = departmentTotals.getOrDefault(department, BigDecimal.ZERO)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    // Use daily budget for single-day reports (if available, otherwise use monthly)
                    // Convert to USD using department's currency exchange rate
                    BigDecimal budgetInDepartmentCurrency = department.getDailyBudget() != null 
                            ? department.getDailyBudget() 
                            : department.getMonthlyBudget();
                    
                    BigDecimal exchangeRate = department.getCurrency().getExchangeRate();
                    BigDecimal budgetInUsd = budgetInDepartmentCurrency
                            .divide(exchangeRate, 2, RoundingMode.HALF_UP);
                    
                    BigDecimal remaining;
                    BigDecimal overBudget;
                    
                    if (used.compareTo(budgetInUsd) <= 0) {
                        // Within budget
                        remaining = budgetInUsd.subtract(used).setScale(2, RoundingMode.HALF_UP);
                        overBudget = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    } else {
                        // Over budget
                        remaining = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                        overBudget = used.subtract(budgetInUsd).setScale(2, RoundingMode.HALF_UP);
                    }
                    
                    return DepartmentExpenseReportResponse.builder()
                            .department(department.getName())
                            .used(used)
                            .remaining(remaining)
                            .overBudget(overBudget)
                            .build();
                })
                .sorted((a, b) -> b.getUsed().compareTo(a.getUsed()))
                .collect(Collectors.toList());
        
        return report;
    }

    /**
     * Generates CSV content from department report data.
     * 
     * @param report the report data
     * @return CSV formatted string
     */
    private String generateDepartmentCsv(List<DepartmentExpenseReportResponse> report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Department,Used (USD),Remaining (USD),Over Budget (USD)\n");
        
        for (DepartmentExpenseReportResponse row : report) {
            csv.append(escapeCsv(row.getDepartment()))
               .append(",")
               .append(row.getUsed())
               .append(",")
               .append(row.getRemaining())
               .append(",")
               .append(row.getOverBudget())
               .append("\n");
        }
        
        return csv.toString();
    }

    /**
     * Escapes CSV values that contain special characters.
     * 
     * @param value the value to escape
     * @return escaped value wrapped in quotes if necessary
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Generates an expense summary based on the current user's role.
     * - EMPLOYEE: returns personal expenses only
     * - MANAGER/FINANCE: returns all expenses across the organization
     * 
     * Includes total expenses, approved expense count, pending expense count, expenses this month,
     * and the last 3 expenses. All amounts are converted to USD.
     *
     * @return expense summary
     */
    @Transactional(readOnly = true)
    public PersonalExpenseSummaryResponse getExpenseSummary() {
        User currentUser = getCurrentUser();
        
        // Check user role to determine scope
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            return getPersonalExpenseSummary(currentUser);
        } else {
            return getOverallExpenseSummary();
        }
    }

    /**
     * Generates personal expense summary for a specific user.
     *
     * @param user the user
     * @return personal expense summary
     */
    private PersonalExpenseSummaryResponse getPersonalExpenseSummary(User user) {
        Long userId = user.getId();
        
        log.info("Generating personal expense summary for user {}", userId);
        
        // Get all expenses for the user (excluding REJECTED)
        List<Expense> allExpenses = expenseRepository.findAllByUserIdAndStatusNot(userId, ExpenseStatus.REJECTED);
        
        // Get last 3 expenses for the user
        List<Expense> recentExpenses = expenseRepository.findTopByUserIdAndStatusNotOrderByExpenseDateDesc(
                userId, ExpenseStatus.REJECTED, PageRequest.of(0, 3));
        
        // Get this month's expenses for the user
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        List<Expense> thisMonthExpenses = expenseRepository.findAllByUserIdAndExpenseDateBetweenAndStatusNot(
                userId, startOfMonth, endOfMonth, ExpenseStatus.REJECTED);
        
        PersonalExpenseSummaryResponse summary = buildExpenseSummary(allExpenses, thisMonthExpenses, recentExpenses);
        
        log.info("Personal summary generated: total={}, approvedCount={}, pendingCount={}, thisMonth={}, lastExpenses={}",
                summary.getTotalExpenses(), summary.getApprovedExpensesCount(), 
                summary.getPendingExpensesCount(), summary.getExpensesThisMonth(), 
                summary.getLastExpenses().size());
        
        return summary;
    }

    /**
     * Generates overall expense summary for all employees.
     *
     * @return overall expense summary
     */
    private PersonalExpenseSummaryResponse getOverallExpenseSummary() {
        log.info("Generating overall expense summary for all users");
        
        // Get all expenses (excluding REJECTED)
        List<Expense> allExpenses = expenseRepository.findAllByStatusNot(ExpenseStatus.REJECTED);
        
        // Get last 3 expenses
        List<Expense> recentExpenses = expenseRepository.findTopByStatusNotOrderByExpenseDateDesc(
                ExpenseStatus.REJECTED, PageRequest.of(0, 3));
        
        // Get this month's expenses
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        List<Expense> thisMonthExpenses = expenseRepository.findAllByExpenseDateBetweenAndStatusNot(
                startOfMonth, endOfMonth, ExpenseStatus.REJECTED);
        
        PersonalExpenseSummaryResponse summary = buildExpenseSummary(allExpenses, thisMonthExpenses, recentExpenses);
        
        log.info("Overall summary generated: total={}, approvedCount={}, pendingCount={}, thisMonth={}, lastExpenses={}",
                summary.getTotalExpenses(), summary.getApprovedExpensesCount(), 
                summary.getPendingExpensesCount(), summary.getExpensesThisMonth(), 
                summary.getLastExpenses().size());
        
        return summary;
    }

    /**
     * Builds an expense summary from the given expense lists.
     * Helper method to avoid code duplication.
     *
     * @param allExpenses all expenses (excluding REJECTED)
     * @param thisMonthExpenses expenses for the current month
     * @param recentExpenses the most recent expenses
     * @return expense summary response
     */
    private PersonalExpenseSummaryResponse buildExpenseSummary(
            List<Expense> allExpenses,
            List<Expense> thisMonthExpenses,
            List<Expense> recentExpenses) {
        
        // Calculate total expenses in USD
        BigDecimal totalExpenses = calculateTotalInUsd(allExpenses);
        
        // Count approved expenses (APPROVED_BY_FINANCE)
        Integer approvedExpensesCount = (int) allExpenses.stream()
                .filter(expense -> expense.getStatus() == ExpenseStatus.APPROVED_BY_FINANCE)
                .count();
        
        // Count pending expenses (PENDING, APPROVED_BY_MANAGER)
        Integer pendingExpensesCount = (int) allExpenses.stream()
                .filter(expense -> expense.getStatus() == ExpenseStatus.PENDING ||
                                 expense.getStatus() == ExpenseStatus.APPROVED_BY_MANAGER)
                .count();
        
        // Calculate this month's expenses in USD
        BigDecimal expensesThisMonth = calculateTotalInUsd(thisMonthExpenses);
        
        // Convert recent expenses to DTOs
        List<LastExpenseDto> lastExpenses = recentExpenses.stream()
                .map(expense -> LastExpenseDto.builder()
                        .description(expense.getDescription())
                        .date(expense.getExpenseDate())
                        .status(expense.getStatus())
                        .build())
                .collect(Collectors.toList());
        
        return PersonalExpenseSummaryResponse.builder()
                .totalExpenses(totalExpenses)
                .approvedExpensesCount(approvedExpensesCount)
                .pendingExpensesCount(pendingExpensesCount)
                .expensesThisMonth(expensesThisMonth)
                .lastExpenses(lastExpenses)
                .build();
    }

    /**
     * Calculates the total amount of expenses in USD.
     *
     * @param expenses list of expenses
     * @return total amount in USD
     */
    private BigDecimal calculateTotalInUsd(List<Expense> expenses) {
        return expenses.stream()
                .map(CurrencyConverter::convertToUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Gets the currently authenticated user from the security context.
     *
     * @return the current user
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
