import {
  BarChart,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Bar,
  ResponsiveContainer,
  Cell,
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

const COLORS = [
  "#8b5cf6",
  "#6366f1",
  "#3b82f6",
  "#06b6d4",
  "#10b981",
  "#f59e0b",
  "#ef4444",
  "#ec4899",
];

type TooltipProps = {
  active?: boolean;
  payload?: Array<{
    value: number;
    payload: HorizontalChart;
  }>;
};

const CustomTooltip = ({ active, payload }: TooltipProps) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg px-3 py-2">
        <p className="text-sm font-semibold text-gray-900 dark:text-white">
          {payload[0].payload.label}
        </p>
        <p className="text-sm text-gray-600 dark:text-gray-400">
          ${payload[0].value.toFixed(2)}
        </p>
      </div>
    );
  }
  return null;
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
      <BarChart data={data} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" className="dark:stroke-gray-700" />
        <XAxis type="number" stroke="#6b7280" className="dark:stroke-gray-400" />
        <YAxis 
          type="category" 
          dataKey="label" 
          width={140} 
          stroke="#6b7280" 
          className="dark:stroke-gray-400"
          style={{ fontSize: '12px' }}
        />
        <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(99, 102, 241, 0.1)' }} />
        <Bar dataKey="value" radius={[0, 8, 8, 0]}>
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
