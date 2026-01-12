package com.ubs.expensemanager.dto.response;

import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO Response representing an Alert returned by the API.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertResponse {

    @Schema(description = "Alert identifier", example = "1")
    private Long id;

    @Schema(description = "Type of the alert", example = "CATEGORY")
    private AlertType type;

    @Schema(description = "Alert message content", example = "Budget exceeded for Food category")
    private String message;

    @Schema(description = "Current status of the alert", example = "NEW")
    private AlertStatus status;

    @Schema(description = "ID of the associated expense", example = "1")
    private Long expenseId;

    @Schema(description = "Description of the associated expense", example = "Team lunch")
    private String expenseDescription;

    @Schema(description = "Timestamp when the alert was created", example = "2026-01-08T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the alert was last updated", example = "2026-01-08T10:15:30")
    private LocalDateTime updatedAt;
}
