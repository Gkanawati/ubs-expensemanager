package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.model.Alert;
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
                .expenseStatus(alert.getExpense().getStatus().toString())
                .build();
    }
}