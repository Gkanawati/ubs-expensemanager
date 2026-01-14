import api from "@/services/api";
import { ExpenseStatus } from "./expense.api";

/**
 * Last expense entry for dashboard
 */
export interface LastExpense {
  description: string;
  date: string;
  status: ExpenseStatus;
}

/**
 * Expenses summary for dashboard
 */
export interface ExpensesSummary {
  totalExpenses: number;
  approvedExpensesCount: number;
  pendingExpensesCount: number;
  expensesThisMonth: number;
  lastExpenses: LastExpense[];
}

/**
 * Fetch expenses summary for dashboard
 */
export async function getExpensesSummary(): Promise<ExpensesSummary> {
  const response = await api.get<ExpensesSummary>("/api/reports/expenses/summary");
  return response.data;
}
