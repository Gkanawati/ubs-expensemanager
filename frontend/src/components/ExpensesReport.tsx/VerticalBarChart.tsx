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
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="label" />
        <YAxis />
        <Tooltip />
        <Legend />

        {series.map(s => (
          <Bar
            key={s.key}
            dataKey={s.key}
            stackId="stack"
            name={s.label}
            fill={s.color}
          />
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}
