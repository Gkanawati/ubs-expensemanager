package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.AlertUpdateRequest;
import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.dto.response.AlertResponse;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.mapper.AlertMapper;
import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    @InjectMocks
    private AlertService alertService;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    private Alert alert;
    private Alert alert2;
    private Expense expense;
    private User employee;
    private AlertResponse alertResponse;
    private AlertListResponse alertListResponse;

    @BeforeEach
    void setUp() {
        // Setup User
        employee = User.builder()
                .id(1L)
                .name("John Employee")
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
                .active(true)
                .build();

        // Setup Currency
        Currency usdCurrency = Currency.builder()
                .id(1L)
                .name("USD")
                .exchangeRate(BigDecimal.ONE)
                .build();

        // Setup ExpenseCategory
        ExpenseCategory foodCategory = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(BigDecimal.valueOf(100))
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency(usdCurrency)
                .build();

        // Setup Expense
        expense = Expense.builder()
                .id(101L)
                .amount(BigDecimal.valueOf(150.50))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.PENDING)
                .build();

        // Setup Alert
        alert = Alert.builder()
                .id(1L)
                .type(AlertType.CATEGORY)
                .message("Daily budget exceeded for category 'Food' on 2026-01-08")
                .status(AlertStatus.NEW)
                .expense(expense)
                .build();

        alert2 = Alert.builder()
                .id(2L)
                .type(AlertType.DEPARTMENT)
                .message("Daily department budget exceeded for department 'IT' on 2026-01-08")
                .status(AlertStatus.NEW)
                .expense(expense)
                .build();

        // Setup AlertResponse
        alertResponse = AlertResponse.builder()
                .id(1L)
                .type(AlertType.CATEGORY)
                .message("Daily budget exceeded for category 'Food' on 2026-01-08")
                .status(AlertStatus.RESOLVED)
                .expenseId(101L)
                .expenseDescription("Team lunch at restaurant")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup AlertListResponse
        alertListResponse = AlertListResponse.builder()
                .idAlert(1L)
                .expenseValue(BigDecimal.valueOf(150.50))
                .currencyName("USD")
                .alertType("Category")
                .alertMessage("Daily budget exceeded for category 'Food' on 2026-01-08")
                .employeeName("John Employee")
                .expenseStatus("PENDING")
                .build();
    }

    @Test
    void findAllPaginated_success_returnsPageOfAlertListResponses() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Alert> alertPage = new PageImpl<>(List.of(alert, alert2), pageable, 2);

        when(alertRepository.findAll(pageable)).thenReturn(alertPage);

        // When
        Page<AlertListResponse> result = alertService.findAllPaginated(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(alertRepository).findAll(pageable);
    }

    @Test
    void resolveAlert_success_returnsResolvedAlert() {
        // Given
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);
        when(alertMapper.updateEntity(eq(alert), any(AlertUpdateRequest.class), eq(expense))).thenReturn(alert);
        when(alertMapper.toResponse(alert)).thenReturn(alertResponse);

        // When
        AlertResponse result = alertService.resolveAlert(1L);

        // Then
        assertNotNull(result);
        assertEquals(AlertStatus.RESOLVED, result.getStatus());
        verify(alertRepository).findById(1L);
        verify(alertRepository).save(any(Alert.class));
        verify(alertMapper).updateEntity(eq(alert), any(AlertUpdateRequest.class), eq(expense));
        verify(alertMapper).toResponse(alert);
    }

    @Test
    void resolveAlert_alertNotFound_throwsResourceNotFoundException() {
        // Given
        when(alertRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> alertService.resolveAlert(999L));
        verify(alertRepository).findById(999L);
        verify(alertRepository, never()).save(any(Alert.class));
    }
}
