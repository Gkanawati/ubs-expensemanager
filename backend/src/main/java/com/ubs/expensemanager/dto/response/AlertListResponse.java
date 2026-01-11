package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO Response representing an Alert in the paginated list returned by the API.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertListResponse {

    @Schema(description = "Alert identifier", example = "1")
    private Long idAlert;

    @Schema(description = "Expense value", example = "150.00")
    private BigDecimal expenseValue;

    @Schema(description = "Currency name", example = "USD")
    private String currencyName;

    @Schema(description = "Alert type", example = "Categoria")
    private String alertType;

    @Schema(description = "Alert message", example = "Budget exceeded for Food category")
    private String alertMessage;

    @Schema(description = "Employee name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Expense status", example = "PENDING")
    private String expenseStatus;
}