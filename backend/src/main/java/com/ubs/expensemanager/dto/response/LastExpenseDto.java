package com.ubs.expensemanager.dto.response;

import com.ubs.expensemanager.model.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO representing a recent expense with basic information.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LastExpenseDto {

    @Schema(description = "Expense description", example = "Team lunch at restaurant")
    private String description;

    @Schema(description = "Date when the expense occurred", example = "2026-01-08")
    private LocalDate date;

    @Schema(description = "Current status of the expense", example = "PENDING")
    private ExpenseStatus status;
}
