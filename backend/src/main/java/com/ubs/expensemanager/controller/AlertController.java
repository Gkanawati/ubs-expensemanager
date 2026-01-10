package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing alerts.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alerts", description = "API for managing alerts")
@PreAuthorize("hasRole('FINANCE')")
public class AlertController {

    private final AlertService alertService;

    @Operation(
        summary = "List Alerts",
        description = "Retrieves a paginated list of alerts with details about the associated expenses."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Alerts retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
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
    @GetMapping("/list")
    public ResponseEntity<Page<AlertListResponse>> listAlerts(
        @PageableDefault(size = 10, sort = "id") @ParameterObject Pageable pageable
    ) {
        log.info("Retrieving paginated alerts with page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<AlertListResponse> alerts = alertService.findAllPaginated(pageable);
        return ResponseEntity.ok(alerts);
    }
}
