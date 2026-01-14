package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response for expense report grouped by category.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryExpenseReportResponse {

    @Schema(description = "Category name", example = "Food")
    private String category;

    @Schema(description = "Total expenses in USD", example = "1234.56")
    private BigDecimal total;
}
