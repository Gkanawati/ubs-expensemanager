package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.DepartmentExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.PersonalExpenseSummaryResponse;
import com.ubs.expensemanager.service.ReportService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        log.info("Request received for expense report by employee: startDate={}, endDate={}", startDate, endDate);
        
        List<EmployeeExpenseReportResponse> report = reportService.getExpensesByEmployeeReport(startDate, endDate);
        
        log.info("Successfully generated report with {} employees", report.size());
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Download expenses by employee as CSV",
            description = "Generates and downloads a CSV file with total expenses grouped by employee. " +
                    "All amounts are converted to USD for comparison. " +
                    "Defaults: If no dates are provided, uses current month (from day 1 to today). " +
                    "Only MANAGER and FINANCE roles can access this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV file generated successfully",
                    content = @Content(mediaType = "text/csv")
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
    @GetMapping("/expenses/by-employee/csv")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<String> getExpensesByEmployeeCsv(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received for CSV expense report by employee: startDate={}, endDate={}", startDate, endDate);
        
        String csv = reportService.getExpensesByEmployeeCsvReport(startDate, endDate);
        String filename = reportService.generateCsvFilename("expenses-by-employee", startDate, endDate);
        
        log.info("Successfully generated CSV report: {}", filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(
            summary = "Get expense report by category",
            description = "Returns total expenses grouped by category within a date range. All amounts are converted to USD."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
    @GetMapping("/expenses/by-category")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<List<CategoryExpenseReportResponse>> getExpensesByCategory(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received for expense report by category: startDate={}, endDate={}", startDate, endDate);
        
        List<CategoryExpenseReportResponse> report = reportService.getExpensesByCategoryReport(startDate, endDate);
        
        log.info("Successfully generated report with {} categories", report.size());
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Download expenses by category as CSV",
            description = "Generates and downloads a CSV file with total expenses grouped by category. " +
                    "All amounts are converted to USD for comparison. " +
                    "Defaults: If no dates are provided, uses current month (from day 1 to today). " +
                    "Only MANAGER and FINANCE roles can access this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV file generated successfully",
                    content = @Content(mediaType = "text/csv")
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
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/expenses/by-category/csv")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<String> getExpensesByCategoryCsv(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received for CSV expense report by category: startDate={}, endDate={}", startDate, endDate);
        
        String csv = reportService.getExpensesByCategoryCsvReport(startDate, endDate);
        String filename = reportService.generateCsvFilename("expenses-by-category", startDate, endDate);
        
        log.info("Successfully generated CSV report: {}", filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(
            summary = "Get expense report by department with budget tracking",
            description = "Generates a report showing department expenses with budget comparison (used, remaining, overBudget). " +
                    "All amounts are converted to USD. " +
                    "Single day reports (when startDate equals endDate) use daily budget for comparison. " +
                    "Period reports use monthly budget and must be within the same month and year (e.g., 2025-01-01 to 2025-01-25 is valid, but 2025-01-01 to 2025-02-01 is not). " +
                    "Defaults to first day of current month until today if no dates are provided (e.g., on 2025-01-13 defaults to [2025-01-01, 2025-01-13]."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/department/budgets-vs-expenses")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<List<DepartmentExpenseReportResponse>> getExpensesByDepartment(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received for expense report by department: startDate={}, endDate={}", startDate, endDate);
        
        List<DepartmentExpenseReportResponse> report = reportService.getExpensesByDepartmentReport(startDate, endDate);
        
        log.info("Successfully generated report with {} departments", report.size());
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Download department budget vs expenses as CSV",
            description = "Generates and downloads a CSV file with department expenses and budget comparison. " +
                    "All amounts are converted to USD. " +
                    "Single day reports (when startDate equals endDate) use daily budget for comparison. " +
                    "Period reports use monthly budget and must be within the same month and year. " +
                    "Defaults to first day of current month until today if no dates are provided. " +
                    "Only MANAGER and FINANCE roles can access this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV file generated successfully",
                    content = @Content(mediaType = "text/csv")
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
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/department/budgets-vs-expenses/csv")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
    public ResponseEntity<String> getExpensesByDepartmentCsv(
            @Parameter(description = "Start date (inclusive). Defaults to first day of current month.", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive). Defaults to current date.", example = "2026-01-13")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received for CSV expense report by department: startDate={}, endDate={}", startDate, endDate);
        
        String csv = reportService.getExpensesByDepartmentCsvReport(startDate, endDate);
        String filename = reportService.generateCsvFilename("expenses-by-department", startDate, endDate);
        
        log.info("Successfully generated CSV report: {}", filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(
            summary = "Get expense summary",
            description = "Returns an expense summary based on the current user's role. " +
                    "EMPLOYEE: returns personal expenses only. " +
                    "MANAGER/FINANCE: returns all expenses across the organization. " +
                    "Includes: total expenses (all time), count of approved expenses (APPROVED_BY_FINANCE), " +
                    "count of pending expenses (PENDING, APPROVED_BY_MANAGER), " +
                    "expenses this month, and the last 3 expenses. All amounts are converted to USD."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense summary generated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalExpenseSummaryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/expenses/summary")
    public ResponseEntity<PersonalExpenseSummaryResponse> getExpenseSummary() {
        log.info("Request received for expense summary");
        
        PersonalExpenseSummaryResponse summary = reportService.getExpenseSummary();
        
        log.info("Successfully generated expense summary");
        return ResponseEntity.ok(summary);
    }
}
