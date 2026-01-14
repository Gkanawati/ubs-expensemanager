package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response representing employee expense report data for horizontal bar charts.
 * 
 * <p>This format is designed to be directly consumed by Recharts horizontal bar chart component.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeExpenseReportResponse {

    @Schema(description = "Employee name", example = "John Doe")
    private String employee;

    @Schema(description = "Total expense amount in USD", example = "3500.50")
    private BigDecimal total;
}
