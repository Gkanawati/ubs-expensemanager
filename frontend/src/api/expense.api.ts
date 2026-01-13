import api from "@/services/api";
import { PageableResponse } from "@/types/pagination";

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

/**
 * Expense audit history entry
 */
export interface ExpenseAuditEntry {
  id: number;
  amount: number;
  description: string | null;
  expenseDate: string;
  userId: number;
  userName: string;
  expenseCategoryId: number;
  expenseCategoryName: string;
  currencyName: string;
  exchangeRate: number;
  receiptUrl: string | null;
  status: ExpenseStatus;
  revisionNumber: number;
  revisionType: number; // 0: create, 1: update
  revisionDate: string;
  revisionUserEmail: string;
}

/**
 * Fetch paginated list of expenses with optional filters
 */
export async function getExpenses(params: {
  page?: number;
  size?: number;
  sort?: string | string[];
  filters?: ExpenseFilters;
}): Promise<PageableResponse<Expense>> {
  const queryParams = new URLSearchParams();

  if (params.page !== undefined) queryParams.append("page", params.page.toString());
  if (params.size !== undefined) queryParams.append("size", params.size.toString());
  if (params.sort) {
    if (Array.isArray(params.sort)) {
      params.sort.forEach((s) => queryParams.append("sort", s));
    } else {
      queryParams.append("sort", params.sort);
    }
  }

  if (params.filters) {
    const { status, startDate, endDate, expenseCategoryId, userId } = params.filters;
    if (status) queryParams.append("status", status);
    if (startDate) queryParams.append("startDate", startDate);
    if (endDate) queryParams.append("endDate", endDate);
    if (expenseCategoryId !== undefined)
      queryParams.append("expenseCategoryId", expenseCategoryId.toString());
    if (userId !== undefined) queryParams.append("userId", userId.toString());
  }

  const response = await api.get<PageableResponse<Expense>>(
    `/api/expenses?${queryParams.toString()}`
  );
  return response.data;
}

/**
 * Get a single expense by ID
 */
export async function getExpenseById(id: number): Promise<Expense> {
  const response = await api.get<Expense>(`/api/expenses/${id}`);
  return response.data;
}

/**
 * Create a new expense
 */
export async function createExpense(payload: CreateExpensePayload): Promise<Expense> {
  const response = await api.post<Expense>("/api/expenses", payload);
  return response.data;
}

/**
 * Update an existing expense
 */
export async function updateExpense(
  id: number,
  payload: UpdateExpensePayload
): Promise<Expense> {
  const response = await api.put<Expense>(`/api/expenses/${id}`, payload);
  return response.data;
}

/**
 * Delete an expense (only PENDING status allowed)
 */
export async function deleteExpense(id: number): Promise<void> {
  await api.delete(`/api/expenses/${id}`);
}

/**
 * Approve an expense
 */
export async function approveExpense(id: number): Promise<Expense> {
  const response = await api.patch<Expense>(`/api/expenses/${id}/approve`);
  return response.data;
}

/**
 * Reject an expense
 */
export async function rejectExpense(id: number): Promise<Expense> {
  const response = await api.patch<Expense>(`/api/expenses/${id}/reject`);
  return response.data;
}

/**
 * Fetch all expense categories for select dropdown
 */
export async function getExpenseCategories(): Promise<ExpenseCategory[]> {
  const response = await api.get<PageableResponse<ExpenseCategory>>(
    "/api/expense-categories?size=1000"
  );
  return response.data.content;
}

/**
 * Fetch all currencies for select dropdown
 */
export async function getCurrencies(): Promise<Currency[]> {
  const response = await api.get<Currency[]>("/api/currencies");
  return response.data;
}

/**
 * Fetch audit history for an expense
 */
export async function getExpenseAuditHistory(id: number): Promise<ExpenseAuditEntry[]> {
  const response = await api.get<ExpenseAuditEntry[]>(`/api/expenses/${id}/audit`);
  return response.data;
}

