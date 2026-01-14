import { useState, useEffect, useRef } from "react";
import { Check, X, CheckCircle, AlertCircle, Filter, History } from "lucide-react";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { TablePagination } from "@/components/Pagination";
import { DatePicker } from "@/components/ui/date-picker";
import { getErrorMessage } from "@/types/api-error";
import { formatCurrency } from "@/utils/validation";
import { useAuth } from "@/hooks/useAuth";
import {
  Expense,
  ExpenseStatus,
  ExpenseFilters,
  getExpenses,
  approveExpense,
  rejectExpense,
} from "@/api/expense.api";
import { ExpenseHistoryDialog } from "../Expenses/components/ExpenseHistoryDialog";

const STATUS_COLORS: Record<ExpenseStatus, string> = {
  PENDING: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400",
  APPROVED_BY_MANAGER: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400",
  APPROVED_BY_FINANCE: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400",
  REJECTED: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400",
};

const STATUS_LABELS: Record<ExpenseStatus, string> = {
  PENDING: "Pending",
  APPROVED_BY_MANAGER: "Approved (Manager)",
  APPROVED_BY_FINANCE: "Approved (Finance)",
  REJECTED: "Rejected",
};

export const ManageExpensesPage = () => {
  const { user } = useAuth();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [statusFilter, setStatusFilter] = useState<ExpenseStatus | "">("");
  const [startDate, setStartDate] = useState<Date | undefined>(undefined);
  const [endDate, setEndDate] = useState<Date | undefined>(undefined);
  const [filterDropdownOpen, setFilterDropdownOpen] = useState(false);
  const filterDropdownRef = useRef<HTMLDivElement>(null);

  const [openApproveDialog, setOpenApproveDialog] = useState(false);
  const [expenseToApprove, setExpenseToApprove] = useState<Expense | null>(null);
  const [openApproveSuccessDialog, setOpenApproveSuccessDialog] = useState(false);
  const [openApproveErrorDialog, setOpenApproveErrorDialog] = useState(false);
  const [approveErrorMessage, setApproveErrorMessage] = useState("");

  const [openRejectDialog, setOpenRejectDialog] = useState(false);
  const [expenseToReject, setExpenseToReject] = useState<Expense | null>(null);
  const [openRejectSuccessDialog, setOpenRejectSuccessDialog] = useState(false);
  const [openRejectErrorDialog, setOpenRejectErrorDialog] = useState(false);
  const [rejectErrorMessage, setRejectErrorMessage] = useState("");

  const [openHistoryDialog, setOpenHistoryDialog] = useState(false);
  const [historyExpenseId, setHistoryExpenseId] = useState<number | null>(null);

  useEffect(() => {
    fetchExpenses();
  }, [currentPage, statusFilter, startDate, endDate]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;

      // Ignore clicks inside the filter dropdown
      if (filterDropdownRef.current?.contains(target)) {
        return;
      }

      // Ignore clicks inside calendar popovers (they render in a portal)
      if (target.closest('[data-radix-popper-content-wrapper]')) {
        return;
      }

      setFilterDropdownOpen(false);
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const getActiveFilterCount = () => {
    let count = 0;
    if (statusFilter) count++;
    if (startDate) count++;
    if (endDate) count++;
    return count;
  };

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      setError(null);

      const filters: ExpenseFilters = {};
      if (statusFilter) filters.status = statusFilter;
      if (startDate) filters.startDate = startDate.toISOString().split("T")[0];
      if (endDate) filters.endDate = endDate.toISOString().split("T")[0];
      // Note: Not filtering by userId to get all expenses

      const response = await getExpenses({
        page: currentPage - 1,
        size: 10,
        sort: "expenseDate,desc",
        filters,
      });

      setExpenses(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error("Error fetching expenses:", err);
      setError("Failed to load expenses");
    } finally {
      setLoading(false);
    }
  };

  const handleApproveClick = (expense: Expense) => {
    setExpenseToApprove(expense);
    setOpenApproveDialog(true);
  };

  const handleConfirmApprove = async () => {
    if (!expenseToApprove) return;

    try {
      await approveExpense(expenseToApprove.id);
      setOpenApproveDialog(false);
      await fetchExpenses();
      setOpenApproveSuccessDialog(true);
    } catch (err) {
      setOpenApproveDialog(false);
      const errorMsg = getErrorMessage(err);
      setApproveErrorMessage(errorMsg);
      setOpenApproveErrorDialog(true);
      console.error("Error approving expense:", err);
    }
  };

  const handleRejectClick = (expense: Expense) => {
    setExpenseToReject(expense);
    setOpenRejectDialog(true);
  };

  const handleConfirmReject = async () => {
    if (!expenseToReject) return;

    try {
      await rejectExpense(expenseToReject.id);
      setOpenRejectDialog(false);
      await fetchExpenses();
      setOpenRejectSuccessDialog(true);
    } catch (err) {
      setOpenRejectDialog(false);
      const errorMsg = getErrorMessage(err);
      setRejectErrorMessage(errorMsg);
      setOpenRejectErrorDialog(true);
      console.error("Error rejecting expense:", err);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value as ExpenseStatus | "");
    setCurrentPage(1);
  };

  const handleHistoryClick = (expense: Expense) => {
    setHistoryExpenseId(expense.id);
    setOpenHistoryDialog(true);
  };

  const columns: ColumnDef<Expense>[] = [
    {
      key: "userName",
      label: "User",
    },
    {
      key: "expenseDate",
      label: "Date",
      render: (row) => {
        const date = new Date(row.expenseDate);
        return date.toLocaleDateString();
      },
    },
    {
      key: "expenseCategoryName",
      label: "Category",
    },
    {
      key: "amount",
      label: "Amount",
      headerAlign: "right",
      render: (row) => {
        const currency = row.currencyName || 'USD';
        return (
          <span className="text-right block">
            {formatCurrency(row.amount ?? 0, currency)}
          </span>
        )
      },
    },
    {
      key: "currencyName",
      label: "Currency",
    },
    {
      key: "description",
      label: "Description",
      render: (row) => {
        const desc = row.description || "-";
        return (
          <span className="max-w-[200px] truncate block" title={desc}>
            {desc.length > 40 ? `${desc.substring(0, 40)}...` : desc}
          </span>
        );
      },
    },
    {
      key: "status",
      label: "Status",
      render: (row) => (
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[row.status]}`}
        >
          {STATUS_LABELS[row.status]}
        </span>
      ),
    },
  ];

  const actions: RowAction<Expense>[] = [
    {
      label: "History",
      icon: <History className="h-4 w-4" />,
      onClick: handleHistoryClick,
      color: "blue",
    },
    {
      label: "Approve",
      icon: <Check className="h-4 w-4" />,
      onClick: handleApproveClick,
      color: "green",
      shouldShow: (expense) => {
        // Hide if already approved by finance
        if (expense.status === "APPROVED_BY_FINANCE") return false;
        // Hide if rejected
        if (expense.status === "REJECTED") return false;
        // Hide if user is manager and already approved by manager
        if (user?.role === "ROLE_MANAGER" && expense.status === "APPROVED_BY_MANAGER") return false;
        // Hide if user is finance and status is still pending (needs manager approval first)
        if (user?.role === "ROLE_FINANCE" && expense.status === "PENDING") return false;
        return true;
      },
    },
    {
      label: "Reject",
      icon: <X className="h-4 w-4" />,
      onClick: handleRejectClick,
      color: "red",
      shouldShow: (expense) => {
        // Hide if already approved by finance
        if (expense.status === "APPROVED_BY_FINANCE") return false;
        // Hide if rejected
        if (expense.status === "REJECTED") return false;
        // Hide if user is manager and already approved by manager
        if (user?.role === "ROLE_MANAGER" && expense.status === "APPROVED_BY_MANAGER") return false;
        // Hide if user is finance and status is still pending (needs manager approval first)
        if (user?.role === "ROLE_FINANCE" && expense.status === "PENDING") return false;
        return true;
      },
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Manage Expenses
        </h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Review and manage all employee expense submissions
        </p>
      </div>

      <div className="rounded-lg border border-border bg-card p-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="relative" ref={filterDropdownRef}>
            <button
              type="button"
              onClick={() => setFilterDropdownOpen(!filterDropdownOpen)}
              className="flex h-9 items-center gap-2 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors hover:bg-gray-50 dark:hover:bg-gray-800"
            >
              <Filter className="h-4 w-4" />
              <span>
                Filters{getActiveFilterCount() > 0 ? ` (${getActiveFilterCount()})` : ""}
              </span>
            </button>

            {filterDropdownOpen && (
              <div className="absolute top-full left-0 mt-1 z-50 min-w-[280px] rounded-md border border-border bg-popover shadow-lg">
                <div className="px-3 py-2 border-b border-gray-200 dark:border-gray-700">
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Filter by Status</span>
                </div>
                <div className="py-1">
                  {(Object.keys(STATUS_LABELS) as ExpenseStatus[]).map((status) => (
                    <label
                      key={status}
                      className="flex items-center gap-2 px-3 py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800"
                    >
                      <div className="relative flex items-center justify-center">
                        <input
                          type="checkbox"
                          checked={statusFilter === status}
                          onChange={() => {
                            handleStatusFilterChange(statusFilter === status ? "" : status);
                          }}
                          className="h-4 w-4 rounded appearance-none border border-gray-300 dark:border-gray-500 checked:bg-primary checked:border-primary focus:ring-2 focus:ring-primary/20 cursor-pointer"
                        />
                        {statusFilter === status && (
                          <svg
                            className="absolute h-3 w-3 text-primary-foreground pointer-events-none"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={3}
                          >
                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                          </svg>
                        )}
                      </div>
                      <span className="text-sm text-gray-700 dark:text-gray-200">{STATUS_LABELS[status]}</span>
                    </label>
                  ))}
                </div>

                <div className="px-3 py-2 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between">
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Filter by Date</span>
                  {(startDate || endDate) && (
                    <button
                      onClick={() => {
                        setStartDate(undefined);
                        setEndDate(undefined);
                        setCurrentPage(1);
                      }}
                      className="text-xs text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-medium cursor-pointer"
                    >
                      Clear dates
                    </button>
                  )}
                </div>
                <div className="px-3 py-2 space-y-3">
                  <div className="space-y-1">
                    <label className="text-xs text-gray-500 dark:text-gray-400">Start</label>
                    <DatePicker
                      value={startDate}
                      onChange={(date) => {
                        setStartDate(date);
                        setCurrentPage(1);
                      }}
                      maxDate={endDate || new Date()}
                      placeholder="Select start date"
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="text-xs text-gray-500 dark:text-gray-400">End</label>
                    <DatePicker
                      value={endDate}
                      onChange={(date) => {
                        setEndDate(date);
                        setCurrentPage(1);
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
      </div>

      <DataTable
        columns={columns}
        data={expenses}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error ? error : "No expenses found"}
      />

      {!loading && !error && totalElements > 0 && (
        <div className="rounded-lg border border-border bg-card">
          <TablePagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            itemsPerPage={10}
            totalItems={totalElements}
          />
        </div>
      )}

      <ConfirmationDialog
        open={openApproveDialog}
        onOpenChange={setOpenApproveDialog}
        title="Approve Expense"
        description={`Are you sure you want to approve this expense of ${
          expenseToApprove
            ? formatCurrency(expenseToApprove.amount, expenseToApprove.currencyName)
            : ""
        }?`}
        confirmText="Approve"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={handleConfirmApprove}
      />

      <ConfirmationDialog
        open={openApproveSuccessDialog}
        onOpenChange={setOpenApproveSuccessDialog}
        title="Expense Approved"
        description="The expense has been approved successfully."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenApproveSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openApproveErrorDialog}
        onOpenChange={setOpenApproveErrorDialog}
        title="Error Approving Expense"
        description={approveErrorMessage}
        confirmText="Close"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={() => setOpenApproveErrorDialog(false)}
      />

      <ConfirmationDialog
        open={openRejectDialog}
        onOpenChange={setOpenRejectDialog}
        title="Reject Expense"
        description={`Are you sure you want to reject this expense of ${
          expenseToReject
            ? formatCurrency(expenseToReject.amount, expenseToReject.currencyName)
            : ""
        }?`}
        confirmText="Reject"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={handleConfirmReject}
      />

      <ConfirmationDialog
        open={openRejectSuccessDialog}
        onOpenChange={setOpenRejectSuccessDialog}
        title="Expense Rejected"
        description="The expense has been rejected successfully."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenRejectSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openRejectErrorDialog}
        onOpenChange={setOpenRejectErrorDialog}
        title="Error Rejecting Expense"
        description={rejectErrorMessage}
        confirmText="Close"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={() => setOpenRejectErrorDialog(false)}
      />

      <ExpenseHistoryDialog
        open={openHistoryDialog}
        onOpenChange={setOpenHistoryDialog}
        expenseId={historyExpenseId}
      />
    </div>
  );
};
