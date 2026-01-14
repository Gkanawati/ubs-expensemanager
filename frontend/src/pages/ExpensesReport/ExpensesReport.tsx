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
import {
  Tabs,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import { Spinner } from "@/components/ui/spinner";
import { Button } from "@/components/ui/button";

type ReportView = "MONTHLY" | "DAILY";

export const ExpensesReport = () => {
  /* ------------------------------------------------------------------ */
  /* View / Filters */
  /* ------------------------------------------------------------------ */

  const [view, setView] = useState<ReportView>("MONTHLY");
  const [filterDropdownOpen, setFilterDropdownOpen] = useState(false);

  const [startDate, setStartDate] = useState<Date | undefined>();
  const [endDate, setEndDate] = useState<Date | undefined>();

  const isDailyWithoutDates =
    view === "DAILY" && (!startDate || !endDate);

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
  }, [view, startDate, endDate]);

  /* ------------------------------------------------------------------ */
  /* Helpers */
  /* ------------------------------------------------------------------ */

  const buildDateParams = () => {
    if (view === "DAILY" && startDate && endDate) {
      return { startDate, endDate };
    }
    return undefined;
  };

  /* ------------------------------------------------------------------ */
  /* Fetches */
  /* ------------------------------------------------------------------ */

  const fetchEmployeeTotals = async () => {
    if (isDailyWithoutDates) {
      setEmployeeData([]);
      return;
    }

    try {
      setEmployeeLoading(true);
      setEmployeeError(null);

      const response = await getExpensesByEmployee(buildDateParams());

      setEmployeeData(
        response.map(item => ({
          label: item.employeeName,
          value: item.totalAmount,
        }))
      );
    } catch {
      setEmployeeError("Failed to load employee data");
    } finally {
      setEmployeeLoading(false);
    }
  };

  const fetchCategoryTotals = async () => {
    if (isDailyWithoutDates) {
      setCategoryData([]);
      return;
    }

    try {
      setCategoryLoading(true);
      setCategoryError(null);

      const response = await getExpensesByCategory(buildDateParams());

      setCategoryData(
        response.map(item => ({
          label: item.categoryName,
          value: item.totalAmount,
        }))
      );
    } catch {
      setCategoryError("Failed to load category data");
    } finally {
      setCategoryLoading(false);
    }
  };

  const fetchDepartmentBudget = async () => {
    if (isDailyWithoutDates) {
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

      {/* Tabs + Filters (same line) */}
      <div className="flex items-center justify-between">
        <Tabs
          value={view}
          onValueChange={(value: string) => {
            const nextView = value as ReportView;
            setView(nextView);

            if (nextView === "MONTHLY") {
              setStartDate(undefined);
              setEndDate(undefined);
              setFilterDropdownOpen(false);
            }
          }}
        >
          <TabsList>
            <TabsTrigger value="MONTHLY">Monthly</TabsTrigger>
            <TabsTrigger value="DAILY">Daily</TabsTrigger>
          </TabsList>
        </Tabs>

        {view === "DAILY" && (
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
                        setStartDate(undefined);
                        setEndDate(undefined);
                      }}
                      className="text-xs font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400"
                    >
                      Clear
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
        )}
      </div>

      {/* Employee */}
      <section className="rounded-lg border p-4">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Totals by Employee</h2>

          <DownloadButtons
            data={employeeData}
            loading={employeeLoading || isDailyWithoutDates}
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

          {!employeeLoading && !employeeError && (
            <div className="w-full h-full">
              <HorizontalBarChart data={employeeData} />
            </div>
          )}
        </div>

      </section>

      {/* Category */}
      <section className="rounded-lg border p-4 h-">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Totals by Category</h2>

          <DownloadButtons
            data={categoryData}
            loading={categoryLoading || isDailyWithoutDates}
            csvFilename="expenses-by-category.csv"
            jsonFilename="expenses-by-category.json"
            onDownloadCsv={downloadExpensesByCategoryCsv}
          />
        </div>

        <div className="min-h-[380px] flex items-center justify-center">
          {categoryLoading && <Spinner />}
          {categoryError && <span>{categoryError}</span>}

          {!categoryLoading && !categoryError && (
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
            loading={departmentLoading || isDailyWithoutDates}
            csvFilename="department-budget.csv"
            jsonFilename="department-budget.json"
            onDownloadCsv={downloadDepartmentBudgetsVsExpensesCsv}
          />
        </div>

        <div className="min-h-[380px] flex items-center justify-center">
          {departmentLoading && <Spinner />}
          {departmentError && <div>{departmentError}</div>}
          {!departmentLoading && !departmentError && (
            <StackedBarChart
              data={departmentData}
              series={[
                { key: "used", label: "Used", color: "#93c5fd" },
                { key: "remaining", label: "Remaining", color: "#1e40af" },
                { key: "overBudget", label: "Over Budget", color: "#dc2626" },
              ]}
            />
          )}
        </div>
      </section>
    </div>
  );
};
