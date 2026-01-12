import { useState, useEffect, useRef } from "react";
import { Plus, Edit, Trash2, CheckCircle, AlertCircle, Filter } from "lucide-react";
import { ActionButton } from "@/components/ui/ActionButton";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { TablePagination } from "@/components/Pagination";
import { DatePicker } from "@/components/ui/date-picker";
import { getErrorMessage } from "@/types/api-error";
import {
  Expense,
  ExpenseStatus,
  CreateExpensePayload,
  UpdateExpensePayload,
  ExpenseFilters,
  getExpenses,
  createExpense,
  updateExpense,
  deleteExpense,
} from "@/api/expense.api";
import { CreateExpenseDialog } from "./components/CreateExpenseDialog";
import { EditExpenseDialog } from "./components/EditExpenseDialog";

const STATUS_COLORS: Record<ExpenseStatus, string> = {
  PENDING: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400",
  APPROVED_BY_MANAGER: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400",
  APPROVED_BY_FINANCE: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400",
  REJECTED: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400",
  REQUIRES_REVISION: "bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-400",
};

const STATUS_LABELS: Record<ExpenseStatus, string> = {
  PENDING: "Pending",
  APPROVED_BY_MANAGER: "Approved (Manager)",
  APPROVED_BY_FINANCE: "Approved (Finance)",
  REJECTED: "Rejected",
  REQUIRES_REVISION: "Needs Revision",
};

const formatCurrency = (amount: number, currency: string): string => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: currency,
    minimumFractionDigits: 2,
  }).format(amount);
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString + "T00:00:00");
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

export const ExpensesPage = () => {
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

  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState("");
  const [openCreateSuccessDialog, setOpenCreateSuccessDialog] = useState(false);

  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [selectedExpense, setSelectedExpense] = useState<Expense | null>(null);
  const [editErrorMessage, setEditErrorMessage] = useState("");
  const [openEditSuccessDialog, setOpenEditSuccessDialog] = useState(false);

  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [expenseToDelete, setExpenseToDelete] = useState<Expense | null>(null);
  const [openDeleteSuccessDialog, setOpenDeleteSuccessDialog] = useState(false);
  const [openDeleteErrorDialog, setOpenDeleteErrorDialog] = useState(false);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState("");

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

  const handleCreateExpense = async (data: CreateExpensePayload) => {
    try {
      await createExpense(data);
      setOpenCreateDialog(false);
      setCreateErrorMessage("");
      await fetchExpenses();
      setOpenCreateSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setCreateErrorMessage(errorMsg);
      console.error("Error creating expense:", err);
    }
  };

  const handleEditExpense = async (data: UpdateExpensePayload) => {
    if (!selectedExpense) return;

    try {
      await updateExpense(selectedExpense.id, data);
      setOpenEditDialog(false);
      setEditErrorMessage("");
      await fetchExpenses();
      setOpenEditSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setEditErrorMessage(errorMsg);
      console.error("Error updating expense:", err);
    }
  };

  const handleDeleteClick = (expense: Expense) => {
    setExpenseToDelete(expense);
    setOpenDeleteDialog(true);
  };

  const handleConfirmDelete = async () => {
    if (!expenseToDelete) return;

    try {
      await deleteExpense(expenseToDelete.id);
      setOpenDeleteDialog(false);
      await fetchExpenses();
      setOpenDeleteSuccessDialog(true);
    } catch (err) {
      setOpenDeleteDialog(false);
      const errorMsg = getErrorMessage(err);
      setDeleteErrorMessage(errorMsg);
      setOpenDeleteErrorDialog(true);
      console.error("Error deleting expense:", err);
    }
  };

  const handleEdit = (expense: Expense) => {
    setSelectedExpense(expense);
    setEditErrorMessage("");
    setOpenEditDialog(true);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value as ExpenseStatus | "");
    setCurrentPage(1);
  };

  const canEdit = (expense: Expense): boolean => {
    return expense.status === "PENDING" || expense.status === "REQUIRES_REVISION";
  };

  const canDelete = (expense: Expense): boolean => {
    return expense.status === "PENDING";
  };

  const columns: ColumnDef<Expense>[] = [
    {
      key: "expenseDate",
      label: "Date",
      render: (row) => formatDate(row.expenseDate),
    },
    {
      key: "expenseCategoryName",
      label: "Category",
    },
    {
      key: "amount",
      label: "Amount",
      headerAlign: "right",
      render: (row) => (
        <span className="text-right block">
          {new Intl.NumberFormat("en-US", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          }).format(row.amount)}
        </span>
      ),
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
      label: "Edit",
      icon: <Edit className="h-4 w-4" />,
      onClick: handleEdit,
      color: "blue",
      shouldShow: canEdit,
    },
    {
      label: "Delete",
      icon: <Trash2 className="h-4 w-4" />,
      onClick: handleDeleteClick,
      color: "red",
      shouldShow: canDelete,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          My Expenses
        </h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          View and manage your expense submissions
        </p>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
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
              <div className="absolute top-full left-0 mt-1 z-50 min-w-[280px] rounded-md border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-900">
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
                            className="absolute h-3 w-3 text-white pointer-events-none"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={3}
                          >
                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                          </svg>
                        )}
                      </div>
                      <span className="text-sm">{STATUS_LABELS[status]}</span>
                    </label>
                  ))}
                </div>

                <div className="px-3 py-2 border-t border-gray-200 dark:border-gray-700">
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Filter by Date</span>
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

          <ActionButton
            label="Add New Expense"
            icon={<Plus className="h-4 w-4" />}
            onClick={() => {
              setCreateErrorMessage("");
              setOpenCreateDialog(true);
            }}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={expenses}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error ? error : "No expenses found"}
      />

      {!loading && !error && totalElements > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900">
          <TablePagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            itemsPerPage={10}
            totalItems={totalElements}
          />
        </div>
      )}

      <CreateExpenseDialog
        open={openCreateDialog}
        onOpenChange={setOpenCreateDialog}
        onSubmit={handleCreateExpense}
        error={createErrorMessage}
      />

      <ConfirmationDialog
        open={openCreateSuccessDialog}
        onOpenChange={setOpenCreateSuccessDialog}
        title="Expense Created"
        description="Your expense has been submitted successfully."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenCreateSuccessDialog(false)}
      />

      <EditExpenseDialog
        open={openEditDialog}
        onOpenChange={setOpenEditDialog}
        onSubmit={handleEditExpense}
        expense={selectedExpense}
        error={editErrorMessage}
      />

      <ConfirmationDialog
        open={openEditSuccessDialog}
        onOpenChange={setOpenEditSuccessDialog}
        title="Expense Updated"
        description="Your expense has been updated successfully."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenEditSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openDeleteDialog}
        onOpenChange={setOpenDeleteDialog}
        title="Delete Expense"
        description={`Are you sure you want to delete this expense of ${
          expenseToDelete
            ? formatCurrency(expenseToDelete.amount, expenseToDelete.currencyName)
            : ""
        }?`}
        confirmText="Delete"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={handleConfirmDelete}
      />

      <ConfirmationDialog
        open={openDeleteSuccessDialog}
        onOpenChange={setOpenDeleteSuccessDialog}
        title="Expense Deleted"
        description="The expense has been deleted successfully."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenDeleteSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openDeleteErrorDialog}
        onOpenChange={setOpenDeleteErrorDialog}
        title="Error Deleting Expense"
        description={deleteErrorMessage}
        confirmText="Close"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={() => setOpenDeleteErrorDialog(false)}
      />
    </div>
  );
};
