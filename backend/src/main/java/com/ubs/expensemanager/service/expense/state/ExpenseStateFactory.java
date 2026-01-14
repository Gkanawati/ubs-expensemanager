package com.ubs.expensemanager.service.expense.state;

import com.ubs.expensemanager.model.ExpenseStatus;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for retrieving expense state instances based on expense status.
 *
 * <p>This factory manages the mapping between {@link ExpenseStatus} enum values
 * and their corresponding {@link ExpenseState} implementations. All state instances are
 * Spring-managed beans injected via constructor.</p>
 *
 * <p>The factory initializes the status-to-state mapping during the {@link PostConstruct}
 * phase, ensuring the map is populated before any requests are processed.</p>
 *
 * <p>Usage example:
 * <pre>
 * ExpenseState currentState = stateFactory.getState(expense.getStatus());
 * currentState.approve(context);
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class ExpenseStateFactory {

  private final PendingState pendingState;
  private final ApprovedByManagerState approvedByManagerState;
  private final ApprovedByFinanceState approvedByFinanceState;
  private final RejectedState rejectedState;

  private Map<ExpenseStatus, ExpenseState> stateMap;

  /**
   * Initializes the status-to-state mapping after bean construction. Called automatically by Spring
   * after all dependencies are injected.
   */
  @PostConstruct
  public void initialize() {
    stateMap = new EnumMap<>(ExpenseStatus.class);
    stateMap.put(ExpenseStatus.PENDING, pendingState);
    stateMap.put(ExpenseStatus.APPROVED_BY_MANAGER, approvedByManagerState);
    stateMap.put(ExpenseStatus.APPROVED_BY_FINANCE, approvedByFinanceState);
    stateMap.put(ExpenseStatus.REJECTED, rejectedState);
  }

  /**
   * Retrieves the state implementation for the given expense status.
   *
   * @param status the expense status
   * @return the corresponding state implementation
   * @throws IllegalArgumentException if no state is registered for the given status
   */
  public ExpenseState getState(ExpenseStatus status) {
    ExpenseState state = stateMap.get(status);
    return Optional.ofNullable(state)
        .orElseThrow(() -> new IllegalArgumentException("No state found for status: " + status));
  }
}
