/**
 * Status of an expense in the approval workflow
 */
export type ExpenseStatus =
  | "PENDING"
  | "APPROVED_BY_MANAGER"
  | "APPROVED_BY_FINANCE"
  | "REJECTED"
  | "REQUIRES_REVISION";

/**
 * Expense entity returned by the API
 */
export interface Expense {
  id: number;
  amount: number;
  description: string | null;
  expenseDate: string;
  userId: number;
  userName: string;
  userEmail: string;
  expenseCategoryId: number;
  expenseCategoryName: string;
  currencyName: string;
  exchangeRate: number;
  receiptUrl: string | null;
  status: ExpenseStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Payload for creating a new expense
 */
export interface CreateExpensePayload {
  amount: number;
  description?: string;
  expenseDate: string;
  expenseCategoryId: number;
  currencyName: string;
  receiptUrl?: string;
}

/**
 * Payload for updating an existing expense
 */
export interface UpdateExpensePayload {
  amount: number;
  description?: string;
  expenseDate: string;
  expenseCategoryId: number;
  currencyName: string;
  receiptUrl?: string;
}

/**
 * Filters for listing expenses
 */
export interface ExpenseFilters {
  status?: ExpenseStatus;
  startDate?: string;
  endDate?: string;
  expenseCategoryId?: number;
  userId?: number;
}

/**
 * Expense category for select dropdowns
 */
export interface ExpenseCategory {
  id: number;
  name: string;
  dailyBudget: number;
  monthlyBudget: number;
  currencyName: string;
  exchangeRate: number;
}

/**
 * Currency for select dropdowns
 */
export interface Currency {
  id: number;
  name: string;
  exchangeRate: number;
}
