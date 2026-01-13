package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO Response representing an expense summary.
 * 
 * <p>This includes total expenses, approved expense count, pending expense count,
 * expenses this month, and the last 3 expenses. Can be used for personal or general reports.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalExpenseSummaryResponse {

    @Schema(description = "Total expenses (all time) in USD", example = "5420.75")
    private BigDecimal totalExpenses;

    @Schema(description = "Count of approved expenses (APPROVED_BY_FINANCE)", example = "15")
    private Integer approvedExpensesCount;

    @Schema(description = "Count of pending expenses (PENDING, APPROVED_BY_MANAGER, REQUIRES_REVISION)", example = "3")
    private Integer pendingExpensesCount;

    @Schema(description = "Total expenses this month in USD", example = "720.00")
    private BigDecimal expensesThisMonth;

    @Schema(description = "Last 3 expenses")
    private List<LastExpenseDto> lastExpenses;
}
