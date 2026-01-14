import {
  BarChart,
  ResponsiveContainer,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  Bar
} from "recharts";

export type BarSeriesConfig = {
  key: string;
  label: string;
  color: string;
};

export type StackedBar = {
  label: string;
  [seriesKey: string]: number | string;
};

type StackedBarChartProps = {
  data: StackedBar[];
  series: BarSeriesConfig[];
  height?: number;
  emptyMessage?: string;
};

type TooltipProps = {
  active?: boolean;
  payload?: Array<{
    name: string;
    value: number;
    color: string;
  }>;
  label?: string;
};

const CustomTooltip = ({ active, payload, label }: TooltipProps) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-popover border border-border rounded-lg shadow-lg px-3 py-2">
        <p className="text-sm font-semibold text-gray-900 dark:text-white mb-1">
          {label}
        </p>
        {payload.map((entry, index: number) => (
          <p key={index} className="text-xs" style={{ color: entry.color }}>
            {entry.name}: ${entry.value.toFixed(2)}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

export function StackedBarChart({
  data,
  series,
  height = 350,
  emptyMessage = "No data available",
}: StackedBarChartProps) {
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
      <BarChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" className="dark:stroke-gray-700" />
        <XAxis 
          dataKey="label" 
          stroke="#6b7280" 
          className="dark:stroke-gray-400"
          style={{ fontSize: '12px' }}
        />
        <YAxis stroke="#6b7280" className="dark:stroke-gray-400" />
        <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(99, 102, 241, 0.05)' }} />
        <Legend 
          wrapperStyle={{ paddingTop: '10px' }}
          iconType="rect"
          iconSize={10}
        />

        {series.map(s => (
          <Bar
            key={s.key}
            dataKey={s.key}
            stackId="stack"
            name={s.label}
            fill={s.color}
            radius={[4, 4, 0, 0]}
          />
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}
