import { useEffect, useState } from "react";
import {
  CheckCircle,
  Edit,
} from "lucide-react";

import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { TablePagination } from "@/components/Pagination";

import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { ResolveAlertDialog } from "@/components/Alerts/ResolveAlertDialog";
import { getAlerts, resolveAlert } from "@/api/alert.api";
import { getErrorMessage } from "@/types/api-error";
import { Badge } from "@/components/ui/badge";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";

type AlertStatus = "NEW" | "RESOLVED";

export interface Alert {
  idAlert: number;
  expenseValue: number;
  currencyName: "USD" | "BRL";
  alertType: string;
  alertMessage: string;
  employeeName: string;
  alertStatus: AlertStatus;
}

export const AlertsPage = () => {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [openResolveDialog, setOpenResolveDialog] = useState(false);
  const [resolveError, setResolveError] = useState("");

  const [openSuccessDialog, setOpenSuccessDialog] = useState(false);

  useEffect(() => {
    fetchAlerts();
  }, [currentPage]);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await getAlerts({
        page: currentPage - 1,
        size: 10,
      });

      setAlerts(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch {
      setError("Failed to load alerts");
    } finally {
      setLoading(false);
    }
  };

  const handleResolveClick = (alert: Alert) => {
    setSelectedAlert(alert);
    setResolveError("");
    setOpenResolveDialog(true);
  };

  const handleResolveSubmit = async () => {
    if (!selectedAlert) return;

    try {
      await resolveAlert(selectedAlert.idAlert);
      setOpenResolveDialog(false);
      await fetchAlerts();
      setOpenSuccessDialog(true);
    } catch (err) {
      setResolveError(getErrorMessage(err));
    }
  };


  const columns: ColumnDef<Alert>[] = [
    { key: "alertType",
      label: "Alert Type",
      render: (row: Alert) => {
        return <span className="font-medium">{row.alertType}</span>;
      },
    },
    {
      key: "alertMessage",
      label: "Message",
      render: row => (
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <div className="max-w-[280px] truncate text-sm cursor-default">
                {row.alertMessage}
              </div>
            </TooltipTrigger>

            <TooltipContent className="max-w-md break-words">
              {row.alertMessage}
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      ),
    },
    { key: "employeeName", label: "Employee" },
    {
      key: "expenseValue",
      label: "Expense Amount",
      render: row =>
        new Intl.NumberFormat(
          row.currencyName === "BRL" ? "pt-BR" : "en-US",
          {
            style: "currency",
            currency: row.currencyName,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          }
        ).format(row.expenseValue),
    },
    {
      key: "alertStatus",
      label: "Alert Status",
      render: row => {
        const variant =
          row.alertStatus === "RESOLVED"
            ? "success"
            : "warning";

        return <Badge variant={variant}>{row.alertStatus}</Badge>;
      },
    }
  ];

  const actions: RowAction<Alert>[] = [
    {
      label: "Resolve",
      icon: <Edit className="h-4 w-4" />,
      color: "blue",
      onClick: handleResolveClick,
      shouldShow: row => row.alertStatus === "NEW",
    },
  ];

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold">Alert Management</h1>
        <p className="text-sm text-muted-foreground">
          Review and resolve expense alerts
        </p>
      </header>

      {/* Table */}
      <DataTable
        columns={columns}
        data={alerts}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error || "No alerts found"}
      />

      {!loading && !error && totalElements > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900">
          <TablePagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
            itemsPerPage={10}
            totalItems={totalElements}
          />
        </div>
      )}

      <ResolveAlertDialog
        open={openResolveDialog}
        onOpenChange={setOpenResolveDialog}
        selectedAlert={selectedAlert ?? undefined}
        onSubmit={handleResolveSubmit}
        error={resolveError}
      />

      <ConfirmationDialog
        open={openSuccessDialog}
        onOpenChange={setOpenSuccessDialog}
        title="Alert Resolved"
        description="The alert has been successfully resolved."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenSuccessDialog(false)}
      />
    </div>
  );
};
