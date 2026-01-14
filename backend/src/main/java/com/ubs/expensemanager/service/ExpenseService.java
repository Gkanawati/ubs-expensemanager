package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseFilterRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseAuditResponse;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.mapper.ExpenseMapper;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.model.audit.CustomRevisionEntity;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.repository.specification.ExpenseSpecifications;
import com.ubs.expensemanager.service.budget.CategoryBudgetValidationStrategy;
import com.ubs.expensemanager.service.budget.DepartmentBudgetValidationStrategy;
import com.ubs.expensemanager.service.expense.state.ExpenseStateFactory;
import com.ubs.expensemanager.service.expense.state.ExpenseState;
import com.ubs.expensemanager.service.expense.state.StateContext;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for handling business logic related to Expenses.
 *
 * <p>This class orchestrates validation, persistence, transformation,
 * budget validation, and status transitions for expenses.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final CurrencyRepository currencyRepository;
    private final ExpenseMapper expenseMapper;
    private final EntityManager entityManager;
    private final CategoryBudgetValidationStrategy categoryBudgetValidationStrategy;
    private final DepartmentBudgetValidationStrategy departmentBudgetValidationStrategy;
    private final ExpenseStateFactory stateFactory;

    /**
     * Creates a new expense with budget validation.
     *
     * @param request data required to create an expense
     * @return created expense as response DTO
     */
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Creating expense for user {} in category {}", currentUser.getId(), request.getExpenseCategoryId());

        ExpenseCategory category = expenseCategoryRepository.findById(request.getExpenseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        Expense expense = expenseMapper.toEntity(request, currency, category, currentUser, ExpenseStatus.PENDING);

        Expense savedExpense = expenseRepository.save(expense);

        // Perform budget validation (warnings only, does not block creation)
        validateBudget(currentUser.getId(), category, savedExpense, request.getAmount());

        log.info("Expense {} created successfully with status PENDING", savedExpense.getId());

        return expenseMapper.toResponse(savedExpense);
    }

    /**
     * Retrieves all expenses with filtering and pagination.
     * EMPLOYEE role: Only sees own expenses
     * MANAGER/FINANCE: Can see all expenses
     *
     * @param filters optional filters for expenses
     * @param pageable pagination parameters
     * @return page of expenses
     */
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(ExpenseFilterRequest filters, Pageable pageable) {
        User currentUser = getCurrentUser();
        Specification<Expense> spec = Specification.where(null);

        // EMPLOYEE can only see their own expenses
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            spec = spec.and(ExpenseSpecifications.withUserId(currentUser.getId()));
            log.debug("Filtering expenses for EMPLOYEE user {}", currentUser.getId());
        } else if (filters.getUserId() != null) {
            // MANAGER/FINANCE can filter by userId if provided
            spec = spec.and(ExpenseSpecifications.withUserId(filters.getUserId()));
            log.debug("Filtering expenses for user {} (requested by MANAGER/FINANCE)", filters.getUserId());
        }

        spec = spec.and(ExpenseSpecifications.withStatus(filters.getStatus()));
        spec = spec.and(ExpenseSpecifications.withStartDate(filters.getStartDate()));
        spec = spec.and(ExpenseSpecifications.withEndDate(filters.getEndDate()));
        spec = spec.and(ExpenseSpecifications.withExpenseCategoryId(filters.getExpenseCategoryId()));

        return expenseRepository.findAll(spec, pageable).map(expenseMapper::toResponse);
    }

    /**
     * Retrieves a single expense by ID with authorization check.
     * EMPLOYEE: Can only view own expenses
     * MANAGER/FINANCE: Can view any expense
     *
     * @param id expense identifier
     * @return expense as response DTO
     */
    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        validateAccess(expense);
        return expenseMapper.toResponse(expense);
    }

    /**
     * Updates an existing expense.
     * Only allowed for PENDING status.
     * Only the expense owner can update.
     *
     * @param id expense identifier
     * @param request updated expense data
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        // Validate ownership
        validateOwnership(expense);

        // Validate status - only PENDING can be updated
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot update expense with status " + expense.getStatus() + ". Only PENDING expenses can be updated."
            );
        }

        log.info("Updating expense {} by user {}", id, currentUser.getId());

        ExpenseCategory category = expenseCategoryRepository.findById(request.getExpenseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        expense = expenseMapper.updateEntity(expense, request, currency, category, ExpenseStatus.PENDING);

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    /**
     * Deletes an expense.
     * Only allowed for PENDING status.
     * Only the expense owner can delete.
     *
     * @param id expense identifier
     */
    @Transactional
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        validateOwnership(expense);

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot delete expense with status " + expense.getStatus() + ". Only PENDING expenses can be deleted."
            );
        }

        log.info("Deleting expense {} by user {}", id, currentUser.getId());
        expenseRepository.delete(expense);
    }

    /**
     * Approves an expense using the State pattern. Delegates to the current state implementation for
     * authorization and transition logic.
     *
     * @param id expense identifier
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse approve(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        log.info("User {} (role: {}) attempting to approve expense {} with status {}",
            currentUser.getId(), currentUser.getRole(), id, expense.getStatus());

        ExpenseState currentState = stateFactory.getState(expense.getStatus());

        StateContext context = StateContext.builder()
            .expense(expense)
            .currentUser(currentUser)
            .expenseRepository(expenseRepository)
            .build();

        currentState.approve(context);

        return expenseMapper.toResponse(expense);
    }

    /**
     * Rejects an expense using the State pattern. Delegates to the current state implementation for
     * authorization and transition logic.
     *
     * @param id expense identifier
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse reject(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        log.info("User {} (role: {}) attempting to reject expense {} with status {}",
            currentUser.getId(), currentUser.getRole(), id, expense.getStatus());

        ExpenseState currentState = stateFactory.getState(expense.getStatus());

        StateContext context = StateContext.builder()
            .expense(expense)
            .currentUser(currentUser)
            .expenseRepository(expenseRepository)
            .build();

        currentState.reject(context);

        return expenseMapper.toResponse(expense);
    }

    /**
     * Validates budget limits (daily and monthly) for the expense.
     * Logs warnings if budget is exceeded but does not block creation.
     * Uses strategy pattern to separate category and department validation.
     *
     * @param userId user ID
     * @param category expense category
     * @param expense object expense
     * @param newAmount amount of the new expense
     */
    private void validateBudget(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        // Validate category budget limits
        categoryBudgetValidationStrategy.validate(userId, category, expense, newAmount);

        // Validate department budget limits
        departmentBudgetValidationStrategy.validate(userId, category, expense, newAmount);
    }

    /**
     * Gets the currently authenticated user from SecurityContext.
     *
     * @return the current User
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Validates that the current user can access the expense.
     * EMPLOYEE: Can only access own expenses
     * MANAGER/FINANCE: Can access any expense
     *
     * @param expense the expense to check
     * @throws UnauthorizedExpenseAccessException if access is denied
     */
    private void validateAccess(Expense expense) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.EMPLOYEE &&
                !expense.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to access expense {} owned by user {}",
                    currentUser.getId(), expense.getId(), expense.getUser().getId());
            throw new UnauthorizedExpenseAccessException("You do not have permission to access this expense");
        }
    }

    /**
     * Validates that the current user owns the expense.
     * Used for update and delete operations.
     *
     * @param expense the expense to check
     * @throws UnauthorizedExpenseAccessException if ownership check fails
     */
    private void validateOwnership(Expense expense) {
        User currentUser = getCurrentUser();

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to modify expense {} owned by user {}",
                    currentUser.getId(), expense.getId(), expense.getUser().getId());
            throw new UnauthorizedExpenseAccessException("You do not have permission to modify this expense");
        }
    }

    /**
     * Retrieves the complete audit history for an expense (asc order).
     *
     * @param id expense identifier
     * @return list of all audited versions of the expense
     */
    @SuppressWarnings("unchecked")
    public List<ExpenseAuditResponse> getAuditHistory(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found");
        }

        var auditReader = AuditReaderFactory.get(entityManager);

        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(Expense.class, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();

        return results.stream()
                .map(result -> {
                    Expense entity = (Expense) result[0];
                    CustomRevisionEntity revEntity = (CustomRevisionEntity) result[1];
                    Number revNumber = revEntity.getId();
                    long revTimestamp = revEntity.getTimestamp();
                    String revUserEmail = revEntity.getModifiedBy();
                    RevisionType revType = (RevisionType) result[2];

                    return ExpenseAuditResponse.builder()
                            .id(entity.getId())
                            .amount(entity.getAmount())
                            .description(entity.getDescription())
                            .expenseDate(entity.getExpenseDate())
                            .userId(entity.getUser().getId())
                            .userName(entity.getUser().getName())
                            .expenseCategoryId(entity.getExpenseCategory().getId())
                            .expenseCategoryName(entity.getExpenseCategory().getName())
                            .currencyName(entity.getCurrency().getName())
                            .exchangeRate(entity.getCurrency().getExchangeRate())
                            .receiptUrl(entity.getReceiptUrl())
                            .status(entity.getStatus())
                            .revisionNumber(revNumber)
                            .revisionType((short) revType.ordinal())
                            .revisionDate(java.time.LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(revTimestamp), ZoneId.systemDefault()))
                            .revisionUserEmail(revUserEmail)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
