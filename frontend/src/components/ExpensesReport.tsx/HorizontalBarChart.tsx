import {
  BarChart,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Bar,
  ResponsiveContainer,
} from "recharts";

export type HorizontalChart = {
  label: string;
  value: number;
};

type HorizontalBarChartProps = {
  data: HorizontalChart[];
  height?: number;
  emptyMessage?: string;
};

export function HorizontalBarChart({
  data,
  height = 300,
  emptyMessage = "No data available",
}: HorizontalBarChartProps) {
  if (!data || data.length === 0) {
    return (
      <div
        className="flex items-center justify-center text-sm text-muted-foreground"
        style={{ height }}
      >
        {emptyMessage}
      </div>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={data} layout="vertical">
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis type="number" />
        <YAxis type="category" dataKey="label" width={140} />
        <Tooltip />
        <Bar dataKey="value" fill="#6366f1" />
      </BarChart>
    </ResponsiveContainer>
  );
}
