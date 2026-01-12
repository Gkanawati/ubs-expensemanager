package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.AlertUpdateRequest;
import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.dto.response.AlertResponse;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.mapper.AlertMapper;
import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for handling business logic related to Alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;

    /**
     * Retrieves all alerts with pagination.
     *
     * @param pageable pagination parameters
     * @return page of alerts in the format required for the list view
     */
    @Transactional(readOnly = true)
    public Page<AlertListResponse> findAllPaginated(Pageable pageable) {
        log.info("Retrieving paginated alerts with page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());

        return alertRepository.findAll(pageable)
                .map(this::mapToAlertListResponse);
    }

    /**
     * Maps an Alert entity to an AlertListResponse DTO.
     *
     * @param alert the alert entity to map
     * @return the mapped AlertListResponse DTO
     */
    private AlertListResponse mapToAlertListResponse(Alert alert) {
        String alertTypeStr;

        if (alert.getType() == AlertType.CATEGORY) {
            alertTypeStr = "Category";
        } else if (alert.getType() == AlertType.DEPARTMENT) {
            alertTypeStr = "Departament";
        } else {
            alertTypeStr = "Category and departament";
        }

        return AlertListResponse.builder()
                .idAlert(alert.getId())
                .expenseValue(alert.getExpense().getAmount())
                .currencyName(alert.getExpense().getCurrency().getName())
                .alertType(alertTypeStr)
                .alertMessage(alert.getMessage())
                .employeeName(alert.getExpense().getUser().getName())
                .alertStatus(alert.getStatus().toString())
                .build();
    }

    /**
     * Updates an alert's status to RESOLVED.
     *
     * @param id the alert ID
     * @return the updated alert as response DTO
     */
    @Transactional
    public AlertResponse resolveAlert(Long id) {
        log.info("Resolving alert with id={}", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        // Create an update request with RESOLVED status
        AlertUpdateRequest updateRequest = AlertUpdateRequest.builder()
                .status(AlertStatus.RESOLVED)
                .build();

        // Update the alert entity
        Alert updatedAlert = alertMapper.updateEntity(alert, updateRequest, alert.getExpense());

        // Save the updated alert
        updatedAlert = alertRepository.save(updatedAlert);

        log.info("Alert {} status changed to RESOLVED", id);

        return alertMapper.toResponse(updatedAlert);
    }
}
