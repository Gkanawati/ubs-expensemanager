package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.dto.response.AlertResponse;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.AlertService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AlertController}.
 *
 * <p>Tests alert management endpoints using {@link MockMvc}.
 * {@link AlertService} is mocked to isolate controller behavior.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AlertController.class)
class AlertControllerTest {

    private static final String BASE_URL = "/api/alerts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertService alertService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private AlertListResponse alertListResponse;
    private AlertResponse alertResponse;

    @BeforeEach
    void setUp() {
        alertListResponse = AlertListResponse.builder()
                .idAlert(1L)
                .expenseValue(new BigDecimal("150.50"))
                .currencyName("USD")
                .alertType("Category")
                .alertMessage("Daily budget exceeded for category 'Food' on 2026-01-08")
                .employeeName("John Doe")
                .alertStatus("NEW")
                .build();

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
    }

    @Test
    void listAlerts_success_returnsOk() throws Exception {
        // Create a page of alert list responses
        Page<AlertListResponse> page = new PageImpl<>(
                List.of(alertListResponse),
                PageRequest.of(0, 10),
                1
        );

        when(alertService.findAllPaginated(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idAlert").value(1))
                .andExpect(jsonPath("$.content[0].expenseValue").value(150.50))
                .andExpect(jsonPath("$.content[0].currencyName").value("USD"))
                .andExpect(jsonPath("$.content[0].alertType").value("Category"))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].alertStatus").value("NEW"));

        verify(alertService).findAllPaginated(any());
    }

    @Test
    void resolveAlert_success_returnsOk() throws Exception {
        when(alertService.resolveAlert(1L)).thenReturn(alertResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/resolve", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("CATEGORY"))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.expenseId").value(101))
                .andExpect(jsonPath("$.expenseDescription").value("Team lunch at restaurant"));

        verify(alertService).resolveAlert(1L);
    }

    @Test
    void listAlerts_noAlerts_returnsEmptyList() throws Exception {
        // Create an empty page
        Page<AlertListResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(alertService.findAllPaginated(any())).thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(alertService).findAllPaginated(any());
    }
}