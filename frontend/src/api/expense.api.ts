import api from "@/services/api";
import { PageableResponse } from "@/types/pagination";
import {
  Expense,
  CreateExpensePayload,
  UpdateExpensePayload,
  ExpenseFilters,
  ExpenseCategory,
  Currency,
} from "@/types/expense";

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
