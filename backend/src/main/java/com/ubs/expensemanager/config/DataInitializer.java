package com.ubs.expensemanager.config;

import com.ubs.expensemanager.model.*;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.DepartmentRepository;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import com.ubs.expensemanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Initializes basic application data on Spring Boot startup.
 *
 * <p> Creates predefined users with passwords encoded using the configured PasswordEncoder.
 * This is needed because bcrypt uses a random salt, so passwords cannot be reliably
 * inserted directly via SQL. </p>
 */
@Component
@Profile("!test")
public class DataInitializer {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyRepository currencyRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public DataInitializer(UserRepository userRepository, 
                          DepartmentRepository departmentRepository,
                          PasswordEncoder passwordEncoder,
                          CurrencyRepository currencyRepository,
                           ExpenseCategoryRepository expenseCategoryRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyRepository = currencyRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    /**
     * Method executed after Spring Boot initialization.
     * Creates predefined users if they do not already exist in the database.
     */
    @PostConstruct
    public void init() {
        // Initialize currencies first (will create audit records automatically via Envers)
        initializeCurrencies();
        
        // Get USD currency (guaranteed to exist after initializeCurrencies)
        Currency usdCurrency = currencyRepository.findByName("USD")
                .orElseThrow(() -> new IllegalStateException("USD currency should have been created"));
        
        // Create IT department if it doesn't exist
        Department itDepartment = departmentRepository.findByNameIgnoreCase("IT")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder()
                                .name("IT")
                                .monthlyBudget(new BigDecimal("15000.00"))
                                .dailyBudget(new BigDecimal("500.00"))
                                .currency(usdCurrency)
                                .build()
                ));

        List<User> usersWithoutManager = List.of(
                User.builder()
                        .email("finance_it@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.FINANCE)
                        .name("Finance User IT")
                        .department(itDepartment)
                        .build(),
                User.builder()
                        .email("manager_it@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.MANAGER)
                        .name("Manager User IT")
                        .department(itDepartment)
                        .build()
        );

        // Save each user if it does not already exist
        for (User user : usersWithoutManager) {
            userRepository.findByEmail(user.getEmail())
                    .orElseGet(() -> userRepository.save(user));
        }

        User manager = userRepository.findByEmail("manager_it@ubs.com")
                .orElseThrow(() -> new IllegalStateException("Manager should have been created"));

        List<User> employees = List.of(
                User.builder()
                        .name("Employee One IT")
                        .email("employee_it@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.EMPLOYEE)
                        .manager(manager)
                        .department(itDepartment)
                        .build(),
                User.builder()
                        .name("Employee Two IT")
                        .email("employee2_it@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.EMPLOYEE)
                        .manager(manager)
                        .department(itDepartment)
                        .build()
        );

        for (User employee : employees) {
            userRepository.findByEmail(employee.getEmail())
                    .orElseGet(() -> userRepository.save(employee));
        }

        // Create HR department if it doesn't exist
        Department hrDepartment = departmentRepository.findByNameIgnoreCase("HR")
            .orElseGet(() -> departmentRepository.save(
                Department.builder()
                    .name("HR")
                    .monthlyBudget(new BigDecimal("10000.00"))
                    .dailyBudget(new BigDecimal("300.00"))
                    .currency(usdCurrency)
                    .build()
            ));

        List<User> hrUsersWithoutManager = List.of(
            User.builder()
                .email("finance_hr@ubs.com")
                .password(passwordEncoder.encode("123456"))
                .role(UserRole.FINANCE)
                .name("Finance User HR")
                .department(hrDepartment)
                .build(),
            User.builder()
                .email("manager_hr@ubs.com")
                .password(passwordEncoder.encode("123456"))
                .role(UserRole.MANAGER)
                .name("Manager User HR")
                .department(hrDepartment)
                .build()
        );

        for (User user : hrUsersWithoutManager) {
            userRepository.findByEmail(user.getEmail())
                .orElseGet(() -> userRepository.save(user));
        }

        User hrManager = userRepository.findByEmail("manager_hr@ubs.com")
            .orElseThrow(() -> new IllegalStateException("HR Manager should have been created"));

        List<User> hrEmployees = List.of(
            User.builder()
                .name("Employee One HR")
                .email("employee_hr@ubs.com")
                .password(passwordEncoder.encode("123456"))
                .role(UserRole.EMPLOYEE)
                .manager(hrManager)
                .department(hrDepartment)
                .build(),
            User.builder()
                .name("Employee Two HR")
                .email("employee2_hr@ubs.com")
                .password(passwordEncoder.encode("123456"))
                .role(UserRole.EMPLOYEE)
                .manager(hrManager)
                .department(hrDepartment)
                .build()
        );

        for (User employee : hrEmployees) {
            userRepository.findByEmail(employee.getEmail())
                .orElseGet(() -> userRepository.save(employee));
        }

        expenseCategoryRepository.findByNameIgnoreCase("Travelling")
            .orElseGet(() -> expenseCategoryRepository.save(
                ExpenseCategory.builder()
                    .name("Travelling")
                    .monthlyBudget(new BigDecimal("1500.00"))
                    .dailyBudget(new BigDecimal("50.00"))
                    .currency(usdCurrency)
                    .build()
            ));

        expenseCategoryRepository.findByNameIgnoreCase("Utilities")
            .orElseGet(() -> expenseCategoryRepository.save(
                ExpenseCategory.builder()
                    .name("Utilities")
                    .monthlyBudget(new BigDecimal("3500.00"))
                    .dailyBudget(new BigDecimal("70.00"))
                    .currency(usdCurrency)
                    .build()
            ));
    }

    /**
     * Initializes base currencies in the system.
     * USD is the base currency with exchange rate 1.0.
     * BRL is initialized with an approximate exchange rate.
     * 
     * Saving through repository ensures Envers creates audit records automatically.
     */
    private void initializeCurrencies() {
        // Initialize USD (base currency)
        currencyRepository.findByName("USD")
                .orElseGet(() -> currencyRepository.save(
                        Currency.builder()
                                .name("USD")
                                .exchangeRate(new BigDecimal("1.000000"))
                                .build()
                ));

        // Initialize BRL with approximate exchange rate
        // This value should be updated regularly via external API
        currencyRepository.findByName("BRL")
                .orElseGet(() -> currencyRepository.save(
                        Currency.builder()
                                .name("BRL")
                                .exchangeRate(new BigDecimal("5.500000"))
                                .build()
                ));
    }
}
