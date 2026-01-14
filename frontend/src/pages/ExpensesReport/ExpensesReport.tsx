import { useEffect, useState } from "react";
import { Filter } from "lucide-react";

import {
  HorizontalBarChart,
  HorizontalChart,
} from "@/components/ExpensesReport.tsx/HorizontalBarChart";
import {
  StackedBar,
  StackedBarChart,
} from "@/components/ExpensesReport.tsx/VerticalBarChart";
import {
  getExpensesByEmployee,
  getExpensesByCategory,
  getDepartmentBudgetsVsExpenses,
  downloadExpensesByEmployeeCsv,
  downloadExpensesByCategoryCsv,
  downloadDepartmentBudgetsVsExpensesCsv,
} from "@/api/expensesreport.api";
import { DownloadButtons } from "@/components/ExpensesReport.tsx/DownloadButtons";
import { DatePicker } from "@/components/ui/date-picker";
import { Spinner } from "@/components/ui/spinner";
import { Button } from "@/components/ui/button";

export const ExpensesReport = () => {
  /* ------------------------------------------------------------------ */
  /* View / Filters */
  /* ------------------------------------------------------------------ */

  const getDefaultDates = () => {
    const today = new Date();
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    return { start: firstDayOfMonth, end: today };
  };

  const [filterDropdownOpen, setFilterDropdownOpen] = useState(false);
  const defaultDates = getDefaultDates();
  const [startDate, setStartDate] = useState<Date | undefined>(defaultDates.start);
  const [endDate, setEndDate] = useState<Date | undefined>(defaultDates.end);

  const getActiveFilterCount = () => {
    let count = 0;
    if (startDate) count++;
    if (endDate) count++;
    return count;
  };

  /* ------------------------------------------------------------------ */
  /* Data states */
  /* ------------------------------------------------------------------ */

  const [employeeData, setEmployeeData] = useState<HorizontalChart[]>([]);
  const [employeeLoading, setEmployeeLoading] = useState(true);
  const [employeeError, setEmployeeError] = useState<string | null>(null);

  const [categoryData, setCategoryData] = useState<HorizontalChart[]>([]);
  const [categoryLoading, setCategoryLoading] = useState(true);
  const [categoryError, setCategoryError] = useState<string | null>(null);

  const [departmentData, setDepartmentData] = useState<StackedBar[]>([]);
  const [departmentLoading, setDepartmentLoading] = useState(true);
  const [departmentError, setDepartmentError] = useState<string | null>(null);

  /* ------------------------------------------------------------------ */
  /* Effects */
  /* ------------------------------------------------------------------ */

  useEffect(() => {
    fetchEmployeeTotals();
    fetchCategoryTotals();
    fetchDepartmentBudget();
  }, [startDate, endDate]);

  /* ------------------------------------------------------------------ */
  /* Helpers */
  /* ------------------------------------------------------------------ */

  const areDatesInSameMonth = () => {
    if (!startDate || !endDate) return true;
    return (
      startDate.getFullYear() === endDate.getFullYear() &&
      startDate.getMonth() === endDate.getMonth()
    );
  };

  const buildDateParams = () => {
    if (startDate && endDate) {
      return { startDate, endDate };
    }
    return undefined;
  };

  /* ------------------------------------------------------------------ */
  /* Fetches */
  /* ------------------------------------------------------------------ */

  const fetchEmployeeTotals = async () => {
    try {
      setEmployeeLoading(true);
      setEmployeeError(null);

      const response = await getExpensesByEmployee(buildDateParams());

      setEmployeeData(
        response.map(item => ({
          label: item.employee,
          value: item.total,
        }))
      );
    } catch {
      setEmployeeError("Failed to load employee data");
      setEmployeeData([]);
    } finally {
      setEmployeeLoading(false);
    }
  };

  const fetchCategoryTotals = async () => {
    try {
      setCategoryLoading(true);
      setCategoryError(null);

      const response = await getExpensesByCategory(buildDateParams());

      setCategoryData(
        response.map(item => ({
          label: item.category,
          value: item.total,
        }))
      );
    } catch {
      setCategoryError("Failed to load category data");
      setCategoryData([]);
    } finally {
      setCategoryLoading(false);
    }
  };

  const fetchDepartmentBudget = async () => {
    if (!areDatesInSameMonth()) {
      setDepartmentLoading(false);
      setDepartmentError(null);
      setDepartmentData([]);
      return;
    }

    try {
      setDepartmentLoading(true);
      setDepartmentError(null);

      const response =
        await getDepartmentBudgetsVsExpenses(buildDateParams());

      setDepartmentData(
        response.map(item => ({
          label: item.department,
          used: item.used,
          remaining: item.remaining,
          overBudget: item.overBudget,
        }))
      );
    } catch {
      setDepartmentError("Failed to load department budget");
      setDepartmentData([]);
    } finally {
      setDepartmentLoading(false);
    }
  };

  /* ------------------------------------------------------------------ */
  /* UI */
  /* ------------------------------------------------------------------ */

  return (
    <div className="space-y-8">
      {/* Header */}
      <header>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Expenses Reports
        </h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Analysis by selected period
        </p>
      </header>

      {/* Filters */}
      <div className="flex items-center justify-end">
        <div className="relative">
          <Button
            variant="outline"
            onClick={() => setFilterDropdownOpen(prev => !prev)}
          >
            <Filter className="h-4 w-4" />
            <span>
              Filters
              {getActiveFilterCount() > 0 &&
                ` (${getActiveFilterCount()})`}
            </span>
          </Button>

          {filterDropdownOpen && (
            <div className="absolute right-0 top-full mt-1 z-50 min-w-[280px] rounded-md border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-900">
              <div className="px-3 py-2 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
                  Filter by Date
                </span>

                {(startDate || endDate) && (
                  <button
                    onClick={() => {
                      const defaults = getDefaultDates();
                      setStartDate(defaults.start);
                      setEndDate(defaults.end);
                    }}
                    className="text-xs font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400"
                  >
                    Reset
                  </button>
                )}
              </div>

              <div className="px-3 py-3 space-y-3">
                <div className="space-y-1">
                  <label className="text-xs text-gray-500 dark:text-gray-400">
                    Start date
                  </label>
                  <DatePicker
                    value={startDate}
                    onChange={setStartDate}
                    maxDate={endDate || new Date()}
                    placeholder="Select start date"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-xs text-gray-500 dark:text-gray-400">
                    End date
                  </label>
                  <DatePicker
                    value={endDate}
                    onChange={(date) => {
                      setEndDate(date);

                      if (startDate && date) {
                        setFilterDropdownOpen(false);
                      }
                    }}
                    minDate={startDate}
                    maxDate={new Date()}
                    placeholder="Select end date"
                  />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Employee */}
      <section className="rounded-lg border p-4">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Totals by Employee</h2>

          <DownloadButtons
            data={employeeData}
            loading={employeeLoading}
            csvFilename="expenses-by-employee.csv"
            jsonFilename="expenses-by-employee.json"
            onDownloadCsv={downloadExpensesByEmployeeCsv}
          />
        </div>

        <div className="min-h-[380px] flex items-center justify-center">
          {employeeLoading && <Spinner />}

          {employeeError && (
            <span className="text-sm text-red-500">{employeeError}</span>
          )}

          {!employeeLoading && !employeeError && employeeData.length === 0 && (
            <span className="text-sm text-gray-500">No data available</span>
          )}

          {!employeeLoading && !employeeError && employeeData.length > 0 && (
            <div className="w-full h-full">
              <HorizontalBarChart data={employeeData} />
            </div>
          )}
        </div>

      </section>

      {/* Category */}
      <section className="rounded-lg border p-4">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Totals by Category</h2>

          <DownloadButtons
            data={categoryData}
            loading={categoryLoading}
            csvFilename="expenses-by-category.csv"
            jsonFilename="expenses-by-category.json"
            onDownloadCsv={downloadExpensesByCategoryCsv}
          />
        </div>

        <div className="min-h-[380px] flex items-center justify-center">
          {categoryLoading && <Spinner />}

          {categoryError && (
            <span className="text-sm text-red-500">{categoryError}</span>
          )}

          {!categoryLoading && !categoryError && categoryData.length === 0 && (
            <span className="text-sm text-gray-500">No data available</span>
          )}

          {!categoryLoading && !categoryError && categoryData.length > 0 && (
            <div className="w-full h-full">
              <HorizontalBarChart data={categoryData} />
            </div>
          )}
        </div>

      </section>

      {/* Department */}
      <section className="rounded-lg border p-4">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Department Budget</h2>

          <DownloadButtons
            data={departmentData}
            loading={departmentLoading}
            csvFilename="department-budget.csv"
            jsonFilename="department-budget.json"
            onDownloadCsv={downloadDepartmentBudgetsVsExpensesCsv}
          />
        </div>

        <div className="min-h-[380px] flex items-center justify-center">
          {departmentLoading && <Spinner />}

          {departmentError && (
            <span className="text-sm text-red-500">{departmentError}</span>
          )}

          {!departmentLoading && !departmentError && !areDatesInSameMonth() && (
            <div className="flex flex-col items-center justify-center gap-2 text-center px-4">
              <span className="text-sm font-medium text-amber-600 dark:text-amber-500">
                ⚠️ This chart is only available for dates within the same month
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                Please select dates from the same month to view the department budget
              </span>
            </div>
          )}

          {!departmentLoading && !departmentError && areDatesInSameMonth() && departmentData.length === 0 && (
            <span className="text-sm text-gray-500">No data available</span>
          )}

          {!departmentLoading && !departmentError && areDatesInSameMonth() && departmentData.length > 0 && (
            <StackedBarChart
              data={departmentData}
              series={[
                { key: "used", label: "Used", color: "#3b82f6" },
                { key: "remaining", label: "Remaining", color: "#10b981" },
                { key: "overBudget", label: "Over Budget", color: "#ef4444" },
              ]}
            />
          )}
        </div>
      </section>
    </div>
  );
};
