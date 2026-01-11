package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.ubs.expensemanager.config.TestSecurityConfig;
import com.ubs.expensemanager.dto.response.AlertListResponse;
import com.ubs.expensemanager.dto.response.AlertResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.security.JwtUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration test for {@link AlertController}
 */
@Import(TestSecurityConfig.class)
public class AlertControllerAPITest extends ControllerAPITest {

  private static final String BASE_DATASET = "datasets/alert/";

  @Autowired
  private JwtUtil jwtUtil;

  private HttpHeaders headers;

  @BeforeEach
  void init() {
    basePath = "http://localhost:%d/api/alerts";
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  /**
   * Verifies if {@link AlertController#listAlerts} will successfully return alerts when finance user requests.
   */
  @Test
  @DataSet(value = BASE_DATASET + "input/alerts.yml", cleanBefore = true, cleanAfter = true, tableOrdering = {"DEPARTMENTS", "USERS", "CURRENCIES", "EXPENSE_CATEGORIES", "EXPENSES", "ALERTS"})
  void shouldReturnAlertsWhenFinanceUserRequests() {
    // given
    final String endpointPath = getPath();
    authenticateAsFinance();

    // when
    ResponseEntity<RestResponsePage<AlertListResponse>> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<RestResponsePage<AlertListResponse>>() {}
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(2, Objects.requireNonNull(response.getBody()).getContent().size())
    );
  }

  /**
   * Verifies if {@link AlertController#listAlerts} will return 403 when non-finance user requests.
   */
  @Test
  @DataSet(value = BASE_DATASET + "input/alerts.yml", tableOrdering = {"DEPARTMENTS", "USERS", "CURRENCIES", "EXPENSE_CATEGORIES", "EXPENSES", "ALERTS"})
  void shouldReturn403WhenNonFinanceUserRequests() {
    // given
    final String endpointPath = getPath();
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
   * Verifies if {@link AlertController#resolveAlert} will successfully resolve an alert when finance user requests.
   */
  @Test
  @DataSet(value = BASE_DATASET + "input/alerts.yml", tableOrdering = {"DEPARTMENTS", "USERS", "CURRENCIES", "EXPENSE_CATEGORIES", "EXPENSES", "ALERTS"})
  @ExpectedDataSet(BASE_DATASET + "expected/after-resolve-alert.yml")
  void shouldResolveAlertWhenFinanceUserRequests() {
    // given
    final String endpointPath = getPath() + "/1/resolve";
    authenticateAsFinance();

    // when
    ResponseEntity<AlertResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        AlertResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("RESOLVED", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link AlertController#resolveAlert} will return 403 when non-finance user requests.
   */
  @Test
  @DataSet(value = BASE_DATASET + "input/alerts.yml", tableOrdering = {"DEPARTMENTS", "USERS", "CURRENCIES", "EXPENSE_CATEGORIES", "EXPENSES", "ALERTS"})
  @ExpectedDataSet(BASE_DATASET + "input/alerts.yml")
  void shouldReturn403WhenNonFinanceUserTriesToResolveAlert() {
    // given
    final String endpointPath = getPath() + "/1/resolve";
    authenticateAsEmployee();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
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
   * Verifies if {@link AlertController#resolveAlert} will return 404 when alert does not exist.
   */
  @Test
  @DataSet(value = BASE_DATASET + "input/alerts.yml", tableOrdering = {"DEPARTMENTS", "USERS", "CURRENCIES", "EXPENSE_CATEGORIES", "EXPENSES", "ALERTS"})
  @ExpectedDataSet(BASE_DATASET + "input/alerts.yml")
  void shouldReturn404WhenAlertDoesNotExist() {
    // given
    final String endpointPath = getPath() + "/999/resolve";
    authenticateAsFinance();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
    );
  }

  // ==================== HELPER METHODS ====================

  private void authenticateAsEmployee() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User employee = User.builder()
        .email("employee@ubs.com")
        .build();

    String token = jwtUtil.generateToken(employee);
    headers.set("Authorization", "Bearer " + token);
  }

  private void authenticateAsFinance() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User finance = User.builder()
        .email("finance@ubs.com")
        .build();

    String token = jwtUtil.generateToken(finance);
    headers.set("Authorization", "Bearer " + token);
  }

  /**
   * Custom Page implementation for deserializing Spring Data Page responses from REST endpoints in
   * integration tests.
   *
   * <p>Jackson cannot directly deserialize {@link org.springframework.data.domain.Page}
   * because it's an interface. This class provides a concrete implementation that can be used with
   * {@link ParameterizedTypeReference}.</p>
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
