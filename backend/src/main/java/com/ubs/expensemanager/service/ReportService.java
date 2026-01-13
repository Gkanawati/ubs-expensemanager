package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.util.CurrencyConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
     * All amounts are converted to USD for comparison.
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of employee expense totals in USD
     */
    @Transactional(readOnly = true)
    public List<EmployeeExpenseReportResponse> getExpensesByEmployee(LocalDate startDate, LocalDate endDate) {
        log.info("Generating expense report by employee from {} to {}", startDate, endDate);
        
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
        
        log.info("Report generated with {} employees", report.size());
        return report;
    }
}
