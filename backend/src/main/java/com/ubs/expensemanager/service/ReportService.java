package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.util.CurrencyConverter;
import com.ubs.expensemanager.util.DateRangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        String csv = generateCsv(report);
        
        log.info("CSV report generated with {} employees", report.size());
        return csv;
    }

    /**
     * Generates filename for CSV download.
     * 
     * @param startDate start date (nullable, defaults to first day of current month)
     * @param endDate end date (nullable, defaults to current date)
     * @return filename
     */
    public String generateCsvFilename(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("expenses-by-employee_%s_to_%s.csv", 
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
     * Generates CSV content from report data.
     * 
     * @param report the report data
     * @return CSV formatted string
     */
    private String generateCsv(List<EmployeeExpenseReportResponse> report) {
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
}
