package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response for expense report grouped by department with budget tracking.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentExpenseReportResponse {

    @Schema(description = "Department name", example = "IT")
    private String department;

    @Schema(description = "Total expenses used in USD", example = "42000.00")
    private BigDecimal used;

    @Schema(description = "Remaining budget in USD", example = "8000.00")
    private BigDecimal remaining;

    @Schema(description = "Amount over budget in USD", example = "0.00")
    private BigDecimal overBudget;
}
