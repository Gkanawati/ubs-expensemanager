package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.service.ReportService;
import com.ubs.expensemanager.util.DateRangeValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller responsible for expense reports and analytics endpoints.
 *
 * <p>Provides aggregated data for visualization and reporting purposes.
 * All amounts are converted to USD for comparison.</p>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Expense Reports and Analytics Endpoints")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Get expenses by employee",
            description = "Generates a report of total expenses grouped by employee for a given date range. " +
                    "All amounts are converted to USD for comparison. " +
                    "Returns data in a format suitable for horizontal bar charts. " +
                    "Defaults: If no dates are provided, uses current month (from day 1 to today). " +
                    "Only MANAGER and FINANCE roles can access this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Report generated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeExpenseReportResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only MANAGER and FINANCE roles can access reports",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/expenses/by-employee")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<List<EmployeeExpenseReportResponse>> getExpensesByEmployee(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Apply defaults: use current month if not specified
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        log.info("Generating expense report by employee: startDate={}, endDate={}", 
                effectiveStartDate, effectiveEndDate);
        
        // Validate date range using utility class
        DateRangeValidator.validate(effectiveStartDate, effectiveEndDate);
        
        List<EmployeeExpenseReportResponse> report = reportService.getExpensesByEmployee(effectiveStartDate, effectiveEndDate);
        
        log.info("Report generated with {} employees", report.size());
        return ResponseEntity.ok(report);
    }
}
