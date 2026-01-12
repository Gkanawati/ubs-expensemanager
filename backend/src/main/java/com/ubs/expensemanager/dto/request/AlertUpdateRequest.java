package com.ubs.expensemanager.dto.request;

import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertUpdateRequest {

    @Schema(description = "Type of the alert (CATEGORY, DEPARTMENT, ALL)", example = "CATEGORY")
    private AlertType type;

    @Schema(description = "Alert message content", example = "Budget exceeded for Food category")
    @Size(max = 500, message = "message must not exceed 500 characters")
    private String message;

    @Schema(description = "Status of the alert (NEW, RESOLVED)", example = "RESOLVED")
    private AlertStatus status;

    @Schema(description = "ID of the associated expense", example = "1")
    private Long expenseId;
}
