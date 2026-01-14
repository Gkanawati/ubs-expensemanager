import { ReactNode } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface ColumnDef<TData> {
  key: keyof TData;
  label: string;
  render?: (row: TData) => ReactNode;
  width?: string;
  headerAlign?: "left" | "center" | "right";
}

export interface RowAction<TData> {
  label: string;
  icon: ReactNode;
  onClick: (row: TData) => void;
  color?: "blue" | "red" | "green" | "yellow";
  shouldShow?: (row: TData) => boolean;
}

export interface DataTableProps<TData> {
  columns: ColumnDef<TData>[];
  data: TData[];
  actions?: RowAction<TData>[];
  emptyMessage?: string;
  className?: string;
  rowClassName?: string;
}

const colorClasses = {
  blue: "text-blue-600",
  red: "text-red-600",
  green: "text-green-600",
  yellow: "text-yellow-600",
};

function renderCellValue(value: unknown): ReactNode {
  if (value === null || value === undefined) return "-";
  if (typeof value === "string" || typeof value === "number") return value;
  return JSON.stringify(value);
}

export const DataTable = <TData,>({
  columns,
  data,
  actions,
  emptyMessage = "Nenhum dado disponível",
  className = "",
  rowClassName = "",
}: DataTableProps<TData>) => {
  // Check if there are any visible actions for any row
  const hasAnyVisibleActions = actions && actions.length > 0 && data.some((row) =>
    actions.some((action) => !action.shouldShow || action.shouldShow(row))
  );

  return (
    <div
      className={`rounded-lg border border-border bg-card ${className}`}
    >
      <Table>
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
              <TableHead
                key={String(column.key)}
                style={column.width ? { width: column.width } : {}}
                className={column.headerAlign === "right" ? "text-right" : column.headerAlign === "center" ? "text-center" : ""}
              >
                {column.label}
              </TableHead>
            ))}
            {hasAnyVisibleActions && (
              <TableHead className="w-20">Actions</TableHead>
            )}
          </TableRow>
        </TableHeader>

        <TableBody>
          {data.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={columns.length + (hasAnyVisibleActions ? 1 : 0)}
                className="text-center py-8 text-gray-500"
              >
                {emptyMessage}
              </TableCell>
            </TableRow>
          ) : (
            data.map((row, rowIndex) => {
              const visibleActions = actions?.filter(
                (action) => !action.shouldShow || action.shouldShow(row)
              ) || [];

              return (
                <TableRow
                  key={rowIndex}
                  className={`hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors ${rowClassName}`}
                >
                  {columns.map((column) => (
                    <TableCell key={String(column.key)}>
                      {column.render
                        ? column.render(row)
                        : renderCellValue(row[column.key])}
                    </TableCell>
                  ))}

                  {hasAnyVisibleActions && (
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {visibleActions.map((action, actionIndex) => (
                          <button
                            key={actionIndex}
                            onClick={() => action.onClick(row)}
                            className="rounded p-1 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer transition-colors"
                            title={action.label}
                          >
                            <div
                              className={
                                colorClasses[action.color || "blue"]
                              }
                            >
                              {action.icon}
                            </div>
                          </button>
                        ))}
                      </div>
                    </TableCell>
                  )}
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
};

DataTable.displayName = "DataTable";
