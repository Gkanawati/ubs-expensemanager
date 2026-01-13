import { test, expect, Page } from '@playwright/test';
import type {
  Expense,
  ExpenseCategory,
  Currency,
  CreateExpensePayload,
} from '@/api/expense.api';

/**
 * E2E Test: Expense Management
 *
 * This test demonstrates expense creation workflow.
 */

// Generate unique test data for each test run
const timestamp = Date.now();
const testExpense: CreateExpensePayload = {
  amount: 150.0,
  description: `Test Expense ${timestamp}`,
  expenseDate: '2026-01-10',
  expenseCategoryId: 1,
  currencyName: 'USD',
};

// Mock data for categories and currencies
const mockCategories: ExpenseCategory[] = [
  { id: 1, name: 'Travel', dailyBudget: 100, monthlyBudget: 3000, currencyName: 'USD', exchangeRate: 1 },
  { id: 2, name: 'Meals', dailyBudget: 50, monthlyBudget: 1000, currencyName: 'USD', exchangeRate: 1 },
  { id: 3, name: 'Office Supplies', dailyBudget: 30, monthlyBudget: 500, currencyName: 'USD', exchangeRate: 1 },
];

const mockCurrencies: Currency[] = [
  { id: 1, name: 'USD', exchangeRate: 1 },
  { id: 2, name: 'BRL', exchangeRate: 5.5 },
  { id: 3, name: 'EUR', exchangeRate: 0.85 },
];

/**
 * Set up API mocks for expense management operations
 */
async function setupExpenseManagementMocks(page: Page) {
  let expenseIdCounter = 100;
  const expenses: Expense[] = [];

  // Mock currencies endpoint
  await page.route('**/api/currencies', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockCurrencies),
    });
  });

  // Mock expense categories endpoint
  await page.route('**/api/expense-categories?*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: mockCategories,
        totalPages: 1,
        totalElements: mockCategories.length,
        number: 0,
        size: 1000,
        first: true,
        last: true,
        numberOfElements: mockCategories.length,
        empty: false,
      }),
    });
  });

  // Mock get expenses list (pageable with filters)
  await page.route('**/api/expenses?*', async (route) => {
    const url = new URL(route.request().url());
    const status = url.searchParams.get('status');
    const startDate = url.searchParams.get('startDate');
    const endDate = url.searchParams.get('endDate');

    let filteredExpenses = [...expenses];

    // Apply status filter
    if (status) {
      filteredExpenses = filteredExpenses.filter((e) => e.status === status);
    }

    // Apply date filters
    if (startDate) {
      filteredExpenses = filteredExpenses.filter((e) => e.expenseDate >= startDate);
    }
    if (endDate) {
      filteredExpenses = filteredExpenses.filter((e) => e.expenseDate <= endDate);
    }

    // Sort by date descending
    filteredExpenses.sort((a, b) => b.expenseDate.localeCompare(a.expenseDate));

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: filteredExpenses,
        totalPages: Math.ceil(filteredExpenses.length / 10) || 1,
        totalElements: filteredExpenses.length,
        number: 0,
        size: 10,
        first: true,
        last: true,
        numberOfElements: filteredExpenses.length,
        empty: filteredExpenses.length === 0,
      }),
    });
  });

  // Mock create expense
  await page.route('**/api/expenses', async (route) => {
    if (route.request().method() === 'POST') {
      const requestData = route.request().postDataJSON() as CreateExpensePayload;
      const category = mockCategories.find((c) => c.id === requestData.expenseCategoryId);
      const currency = mockCurrencies.find((c) => c.name === requestData.currencyName);

      const newExpense: Expense = {
        id: expenseIdCounter++,
        amount: requestData.amount,
        description: requestData.description || null,
        expenseDate: requestData.expenseDate,
        userId: 1,
        userName: 'Employee User',
        userEmail: 'employee@ubs.com',
        expenseCategoryId: requestData.expenseCategoryId,
        expenseCategoryName: category?.name || 'Unknown',
        currencyName: requestData.currencyName,
        exchangeRate: currency?.exchangeRate || 1,
        receiptUrl: requestData.receiptUrl || null,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      expenses.push(newExpense);

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(newExpense),
      });
    } else {
      await route.continue();
    }
  });

  // Mock login for Employee user
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Login successful',
        token: 'mock-jwt-token',
        user: {
          id: 1,
          email: 'employee@ubs.com',
          role: 'ROLE_EMPLOYEE',
          name: 'Employee User',
        },
      }),
    });
  });

  return { expenses };
}

test.describe('Expense Management', () => {
  test.beforeEach(async ({ page }) => {
    // Set up all mocks
    await setupExpenseManagementMocks(page);

    // Login as employee user
    await page.goto('/');
    await page.getByLabel(/email/i).fill('employee@ubs.com');
    await page.getByLabel(/password/i).fill('Employee123456');
    await page.getByRole('button', { name: /login/i }).click();

    // Navigate to Expenses page
    await page.waitForURL(/\/dashboard/);
    await page.goto('/expenses');
    await expect(page.getByRole('heading', { name: 'My Expenses' })).toBeVisible();
  });

  test('should display empty expenses table initially', async ({ page }) => {
    await expect(page.getByText('No expenses found')).toBeVisible();
  });

  test('should create a new expense', async ({ page }) => {
    // Click Add New Expense button
    await page.getByRole('button', { name: /add new expense/i }).click();

    // Wait for dialog to open
    await expect(page.getByRole('heading', { name: 'Expense Submission' })).toBeVisible();

    // Select category
    await page.locator('#expenseCategoryId').selectOption(testExpense.expenseCategoryId.toString());

    // Select currency
    await page.locator('#currencyName').selectOption(testExpense.currencyName);

    // Fill amount (wait for currency to enable the input)
    const amountInput = page.locator('#amount');
    await expect(amountInput).toBeEnabled();
    await amountInput.fill(testExpense.amount.toString());

    // Fill description
    await page.locator('#description').fill(testExpense.description);

    // Submit form
    const submitButton = page.getByRole('button', { name: /submit/i });
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    // Wait for success dialog
    await expect(page.getByText('Expense Created')).toBeVisible();
    await page.getByRole('button', { name: /done/i }).click();

    // Verify expense appears in the table
    await expect(page.getByText('Travel')).toBeVisible();
    await expect(page.getByText('Pending')).toBeVisible();
  });

  test('should create expense successfully', async ({ page }) => {
    // Create a new expense
    await page.getByRole('button', { name: /add new expense/i }).click();
    await expect(page.getByRole('heading', { name: 'Expense Submission' })).toBeVisible();

    // Fill form
    await page.locator('#expenseCategoryId').selectOption(testExpense.expenseCategoryId.toString());
    await page.locator('#currencyName').selectOption(testExpense.currencyName);

    const amountInput = page.locator('#amount');
    await expect(amountInput).toBeEnabled();
    await amountInput.fill(testExpense.amount.toString());

    await page.locator('#description').fill(testExpense.description);

    // Submit
    await page.getByRole('button', { name: /submit/i }).click();
    await expect(page.getByText('Expense Created')).toBeVisible();
    await page.getByRole('button', { name: /done/i }).click();

    // Verify expense appears in table
    await expect(page.getByText('Travel')).toBeVisible();
    await expect(page.getByText('Pending')).toBeVisible();
  });
});

test.describe('Expense Filters', () => {
  test.beforeEach(async ({ page }) => {
    await setupExpenseManagementMocks(page);

    await page.goto('/');
    await page.getByLabel(/email/i).fill('employee@ubs.com');
    await page.getByLabel(/password/i).fill('Employee123456');
    await page.getByRole('button', { name: /login/i }).click();

    await page.waitForURL(/\/dashboard/);
    await page.goto('/expenses');
    await expect(page.getByRole('heading', { name: 'My Expenses' })).toBeVisible();
  });

  test('should filter expenses by status', async ({ page }) => {
    // Create an expense first
    await page.getByRole('button', { name: /add new expense/i }).click();
    await page.locator('#expenseCategoryId').selectOption('1');
    await page.locator('#currencyName').selectOption('USD');
    await page.locator('#amount').fill('100');
    await page.getByRole('button', { name: /submit/i }).click();
    await expect(page.getByText('Expense Created')).toBeVisible();
    await page.getByRole('button', { name: /done/i }).click();

    // Verify expense exists
    await expect(page.getByText('Travel')).toBeVisible();

    // Open filters dropdown
    await page.getByRole('button', { name: /filters/i }).click();

    // Select PENDING status filter (use label within the filter dropdown)
    await page.locator('label').filter({ hasText: 'Pending' }).click();

    // Verify filter is applied (counter shows 1)
    await expect(page.getByRole('button', { name: /filters \(1\)/i })).toBeVisible();

    // Expense should still be visible since it's PENDING
    await expect(page.getByText('Travel')).toBeVisible();
  });

  test('should show filter counter when filters are active', async ({ page }) => {
    // Open filters dropdown
    await page.getByRole('button', { name: /filters/i }).click();

    // Initially no filters active
    await expect(page.getByRole('button', { name: /^filters$/i })).toBeVisible();

    // Select a status filter (use label within the filter dropdown)
    await page.locator('label').filter({ hasText: 'Pending' }).click();

    // Counter should show (1)
    await expect(page.getByRole('button', { name: /filters \(1\)/i })).toBeVisible();
  });
});
