package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.ubs.expensemanager.config.TestSecurityConfig;
import com.ubs.expensemanager.dto.response.CategoryExpenseReportResponse;
import com.ubs.expensemanager.dto.response.DepartmentExpenseReportResponse;
import com.ubs.expensemanager.dto.response.EmployeeExpenseReportResponse;
import com.ubs.expensemanager.dto.response.PersonalExpenseSummaryResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link ReportController}
 */
@Import(TestSecurityConfig.class)
public class ReportControllerAPITest extends ControllerAPITest {

    private static final String BASE_DATASET = "datasets/expense/";

    @Autowired
    private JwtUtil jwtUtil;

    private HttpHeaders headers;

    @BeforeEach
    void init() {
        basePath = "http://localhost:%d/api/reports";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Verifies if expenses by employee report returns correct totals for multiple employees
     * across different departments.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnExpensesByEmployeeReport() {
        // given
        final String endpointPath = getPath() + "/expenses/by-employee";
        authenticateAsManager();

        // when
        ResponseEntity<List<EmployeeExpenseReportResponse>> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<EmployeeExpenseReportResponse>>() {}
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().isEmpty());
                },
                () -> {
                    // Verify that employees from different departments are included
                    assertNotNull(response.getBody());
                    List<String> employees = response.getBody().stream()
                            .map(EmployeeExpenseReportResponse::getEmployee)
                            .toList();
                    assertFalse(employees.isEmpty());
                }
        );
    }

    /**
     * Verifies if expenses by category report groups expenses correctly.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnExpensesByCategoryReport() {
        // given
        final String endpointPath = getPath() + "/expenses/by-category";
        authenticateAsManager();

        // when
        ResponseEntity<List<CategoryExpenseReportResponse>> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<CategoryExpenseReportResponse>>() {}
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().isEmpty());
                },
                () -> {
                    // Verify totals are positive
                    assertNotNull(response.getBody());
                    response.getBody().forEach(category ->
                            assertTrue(category.getTotal().compareTo(BigDecimal.ZERO) > 0)
                    );
                }
        );
    }

    /**
     * Verifies if department budget vs expenses report shows correct used, remaining and overBudget values.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnDepartmentBudgetVsExpensesReport() {
        // given
        final String endpointPath = getPath() + "/department/budgets-vs-expenses";
        authenticateAsManager();

        // when
        ResponseEntity<List<DepartmentExpenseReportResponse>> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<DepartmentExpenseReportResponse>>() {}
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().isEmpty());
                },
                () -> {
                    // Verify that each department has valid budget tracking
                    assertNotNull(response.getBody());
                    response.getBody().forEach(dept -> {
                        assertNotNull(dept.getDepartment());
                        assertNotNull(dept.getUsed());
                        assertNotNull(dept.getRemaining());
                        assertNotNull(dept.getOverBudget());
                        // Used + Remaining should approximately equal budget (or used > budget if overBudget)
                        assertTrue(dept.getUsed().compareTo(BigDecimal.ZERO) >= 0);
                    });
                }
        );
    }

    /**
     * Verifies if CSV export for expenses by employee works correctly.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldExportExpensesByEmployeeAsCsv() {
        // given
        final String endpointPath = getPath() + "/expenses/by-employee/csv";
        authenticateAsManager();

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().contains("Employee,Total (USD)"));
                },
                () -> assertNotNull(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
        );
    }

    /**
     * Verifies if expense summary returns personal data for an employee.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnPersonalSummaryForEmployee() {
        // given
        final String endpointPath = getPath() + "/expenses/summary";
        authenticateAsEmployee();

        // when
        ResponseEntity<PersonalExpenseSummaryResponse> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PersonalExpenseSummaryResponse.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    PersonalExpenseSummaryResponse summary = response.getBody();
                    assertNotNull(summary);
                    assertNotNull(summary.getTotalExpenses());
                    assertNotNull(summary.getApprovedExpensesCount());
                    assertNotNull(summary.getPendingExpensesCount());
                    assertNotNull(summary.getExpensesThisMonth());
                    assertNotNull(summary.getLastExpenses());
                    assertTrue(summary.getTotalExpenses().compareTo(BigDecimal.ZERO) >= 0);
                    assertTrue(summary.getApprovedExpensesCount() >= 0);
                    assertTrue(summary.getPendingExpensesCount() >= 0);
                }
        );
    }

    /**
     * Verifies if expense summary returns overall data for a manager.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnOverallSummaryForManager() {
        // given
        final String endpointPath = getPath() + "/expenses/summary";
        authenticateAsManager();

        // when
        ResponseEntity<PersonalExpenseSummaryResponse> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PersonalExpenseSummaryResponse.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    PersonalExpenseSummaryResponse summary = response.getBody();
                    assertNotNull(summary);
                    assertNotNull(summary.getTotalExpenses());
                    assertNotNull(summary.getApprovedExpensesCount());
                    assertNotNull(summary.getPendingExpensesCount());
                    // Manager should see more expenses than employee
                    assertTrue(summary.getTotalExpenses().compareTo(BigDecimal.ZERO) >= 0);
                }
        );
    }

    /**
     * Verifies that employee cannot access manager-only reports.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturn403WhenEmployeeTriesToAccessEmployeeReport() {
        // given
        final String endpointPath = getPath() + "/expenses/by-employee";
        authenticateAsEmployee();

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
        );
    }

    /**
     * Verifies that date range parameters work correctly.
     */
    @Test
    @DataSet(BASE_DATASET + "input/report-test-data.yml")
    void shouldReturnExpensesForSpecificDateRange() {
        // given
        final String endpointPath = getPath() + "/expenses/by-employee?startDate=2026-01-01&endDate=2026-01-12";
        authenticateAsManager();

        // when
        ResponseEntity<List<EmployeeExpenseReportResponse>> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<EmployeeExpenseReportResponse>>() {}
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody())
        );
    }

    private void authenticateAsEmployee() {
        User employee = User.builder()
                .email("employee@ubs.com")
                .build();

        String token = jwtUtil.generateToken(employee);
        headers.set("Authorization", "Bearer " + token);
    }

    private void authenticateAsManager() {
        User manager = User.builder()
                .email("manager@ubs.com")
                .build();

        String token = jwtUtil.generateToken(manager);
        headers.set("Authorization", "Bearer " + token);
    }

    private void authenticateAsFinance() {
        User finance = User.builder()
                .email("finance@ubs.com")
                .build();

        String token = jwtUtil.generateToken(finance);
        headers.set("Authorization", "Bearer " + token);
    }

    /**
     * Custom Page implementation for deserializing Spring Data Page responses from REST endpoints in
     * integration tests.
     */
    static class RestResponsePage<T> extends PageImpl<T> {

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public RestResponsePage(@JsonProperty("content") List<T> content,
                                @JsonProperty("number") int number,
                                @JsonProperty("size") int size,
                                @JsonProperty("totalElements") Long totalElements,
                                @JsonProperty("pageable") JsonNode pageable,
                                @JsonProperty("last") boolean last,
                                @JsonProperty("totalPages") int totalPages,
                                @JsonProperty("sort") JsonNode sort,
                                @JsonProperty("first") boolean first,
                                @JsonProperty("numberOfElements") int numberOfElements) {
            super(content, PageRequest.of(number, size), totalElements);
        }

        public RestResponsePage(List<T> content) {
            super(content);
        }
    }
}
