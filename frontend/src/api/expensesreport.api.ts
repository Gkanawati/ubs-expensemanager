import api from "@/services/api";

export type DateFilterParams = {
  startDate: Date;
  endDate: Date;
};

const formatDateOnly = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

const buildParams = (params?: DateFilterParams) => {
  if (!params) return undefined;

  return {
    startDate: formatDateOnly(params.startDate),
    endDate: formatDateOnly(params.endDate),
  };
};

export interface ExpensesByEmployeeReport {
  employee: string;
  total: number;
}

export async function getExpensesByEmployee(
  params?: DateFilterParams
): Promise<ExpensesByEmployeeReport[]> {
  const response = await api.get<ExpensesByEmployeeReport[]>(
    "/api/reports/expenses/by-employee",
    {
      params: buildParams(params),
    }
  );

  return response.data;
}

export async function downloadExpensesByEmployeeCsv(
  params?: DateFilterParams
): Promise<Blob> {
  const response = await api.get(
    "/api/reports/expenses/by-employee/csv",
    {
      params: buildParams(params),
      responseType: "blob",
    }
  );

  return response.data;
}

export interface ExpensesByCategoryReport {
  category: string;
  total: number;
}

export async function getExpensesByCategory(
  params?: DateFilterParams
): Promise<ExpensesByCategoryReport[]> {
  const response = await api.get<ExpensesByCategoryReport[]>(
    "/api/reports/expenses/by-category",
    {
      params: buildParams(params),
    }
  );

  return response.data;
}

export async function downloadExpensesByCategoryCsv(
  params?: DateFilterParams
): Promise<Blob> {
  const response = await api.get(
    "/api/reports/expenses/by-category/csv",
    {
      params: buildParams(params),
      responseType: "blob",
    }
  );

  return response.data;
}

export interface DepartmentBudgetsVsExpensesReport {
  department: string;
  used: number;
  remaining: number;
  overBudget: number;
}

export async function getDepartmentBudgetsVsExpenses(
  params?: DateFilterParams
): Promise<DepartmentBudgetsVsExpensesReport[]> {
  const response = await api.get<DepartmentBudgetsVsExpensesReport[]>(
    "/api/reports/department/budgets-vs-expenses",
    {
      params: buildParams(params),
    }
  );

  return response.data;
}

export async function downloadDepartmentBudgetsVsExpensesCsv(
  params?: DateFilterParams
): Promise<Blob> {
  const response = await api.get(
    "/api/reports/department/budgets-vs-expenses/csv",
    {
      params: buildParams(params),
      responseType: "blob",
    }
  );

  return response.data;
}
