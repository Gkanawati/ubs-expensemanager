import { useState, useEffect } from "react";
import { X, Clock } from "lucide-react";
import { getExpenseAuditHistory, ExpenseAuditEntry } from "@/api/expense.api";
import { formatCurrency, formatDate } from "@/utils/validation";
import { getErrorMessage } from "@/types/api-error";

interface ExpenseHistoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  expenseId: number | null;
}

const REVISION_TYPE_LABELS: Record<number, string> = {
  0: "Create",
  1: "Update",
};

const REVISION_TYPE_COLORS: Record<number, string> = {
  0: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400",
  1: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400",
};

const STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400",
  APPROVED_BY_MANAGER: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400",
  APPROVED_BY_FINANCE: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400",
  REJECTED: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400",
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: "Pending",
  APPROVED_BY_MANAGER: "Approved (Manager)",
  APPROVED_BY_FINANCE: "Approved (Finance)",
  REJECTED: "Rejected",
};

export const ExpenseHistoryDialog = ({
  open,
  onOpenChange,
  expenseId,
}: ExpenseHistoryDialogProps) => {
  const [history, setHistory] = useState<ExpenseAuditEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open && expenseId) {
      fetchHistory();
    }
  }, [open, expenseId]);

  const fetchHistory = async () => {
    if (!expenseId) return;

    try {
      setLoading(true);
      setError(null);
      const data = await getExpenseAuditHistory(expenseId);
      // Sort by revision number descending (newest first)
      setHistory(data.sort((a, b) => b.revisionNumber - a.revisionNumber));
    } catch (err) {
      console.error("Error fetching expense history:", err);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const formatRevisionDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="fixed inset-0 bg-black/50 dark:bg-black/70"
        onClick={() => onOpenChange(false)}
      />

      <div className="relative z-50 w-full max-w-4xl max-h-[90vh] overflow-hidden rounded-lg border border-border bg-card shadow-lg m-4">
        <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4 dark:border-gray-700">
          <div className="flex items-center gap-2">
            <Clock className="h-5 w-5 text-gray-600 dark:text-gray-400" />
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
              Expense History
            </h2>
          </div>
          <button
            onClick={() => onOpenChange(false)}
            className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-800"
          >
            <X className="h-5 w-5 text-gray-500 dark:text-gray-400" />
          </button>
        </div>

        <div className="overflow-y-auto max-h-[calc(90vh-80px)] p-6">
          {loading && (
            <div className="text-center py-8">
              <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]" />
              <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                Loading history...
              </p>
            </div>
          )}

          {error && (
            <div className="rounded-lg bg-red-50 p-4 dark:bg-red-900/20">
              <p className="text-sm text-red-800 dark:text-red-400">{error}</p>
            </div>
          )}

          {!loading && !error && history.length === 0 && (
            <div className="text-center py-8">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                No history found for this expense.
              </p>
            </div>
          )}

          {!loading && !error && history.length > 0 && (
            <div className="space-y-4">
              {history.map((entry, index) => (
                <div
                  key={`${entry.revisionNumber}-${index}`}
                  className="rounded-lg border border-border bg-muted p-4"
                >
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          REVISION_TYPE_COLORS[entry.revisionType]
                        }`}
                      >
                        {REVISION_TYPE_LABELS[entry.revisionType] || "Unknown"}
                      </span>
                      <span className="text-xs text-gray-500 dark:text-gray-400">
                        Revision #{entry.revisionNumber}
                      </span>
                    </div>
                    <span className="text-xs text-gray-500 dark:text-gray-400">
                      {formatRevisionDate(entry.revisionDate)}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Amount
                      </label>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white">
                        {formatCurrency(entry.amount, entry.currencyName)}
                      </p>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Status
                      </label>
                      <div className="mt-1">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            STATUS_COLORS[entry.status]
                          }`}
                        >
                          {STATUS_LABELS[entry.status]}
                        </span>
                      </div>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Date
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {formatDate(entry.expenseDate)}
                      </p>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Category
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {entry.expenseCategoryName}
                      </p>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        User
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {entry.userName}
                      </p>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Currency
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {entry.currencyName}
                      </p>
                    </div>

                    <div>
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Modified By
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {entry.revisionUserEmail}
                      </p>
                    </div>

                    {entry.receiptUrl ? (
                      <div>
                        <label className="text-xs font-medium text-gray-500 dark:text-gray-400 block mb-1">
                          Receipt
                        </label>
                        <a
                          href={entry.receiptUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 underline block"
                        >
                          View Receipt
                        </a>
                      </div>
                    ) : (
                      <div />
                    )}

                    <div className="col-span-2">
                      <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
                        Description
                      </label>
                      <p className="text-sm text-gray-900 dark:text-white">
                        {entry.description || "-"}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
