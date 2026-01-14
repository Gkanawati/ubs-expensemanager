import { AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/pages/Alerts/AlertsPage";

interface ResolveAlertDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;

  selectedAlert?: Alert;

  onSubmit: () => void;
  error?: string;
  isLoading?: boolean;
}

export const ResolveAlertDialog = ({
  open,
  onOpenChange,
  selectedAlert,
  onSubmit,
  error,
  isLoading = false,
}: ResolveAlertDialogProps) => {
  if (!selectedAlert) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="sm:max-w-[500px]"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Resolve Alert</DialogTitle>
          <DialogDescription>
            Review the alert details before resolving
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 mt-0.5" />
            <div>
              <h3 className="text-sm font-medium text-red-800">
                Error resolving alert
              </h3>
              <p className="mt-1 text-sm text-red-700">
                {error}
              </p>
            </div>
          </div>
        )}

        {/* Alert info */}
        <div className="space-y-3 text-sm">
          <div>
            <span className="font-medium">Employee:</span>{" "}
            {selectedAlert.employeeName}
          </div>

          <div>
            <span className="font-medium">Alert Status:</span>{" "}
            {selectedAlert.alertStatus}
          </div>

          <div className="rounded-md bg-muted p-3 text-muted-foreground">
            {selectedAlert.alertMessage}
          </div>
        </div>

        <DialogFooter className="pt-4 gap-2">
          <Button
            variant="outline"
            type="button"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>

          <Button
            type="button"
            onClick={onSubmit}
            loading={isLoading}
          >
            {isLoading ? "Resolving..." : "Resolve"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
