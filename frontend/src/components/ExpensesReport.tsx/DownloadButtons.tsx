import { Download } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";

type DownloadButtonsProps<T> = {
  data: T[];
  loading: boolean;
  csvFilename: string;
  jsonFilename: string;
  onDownloadCsv: () => Promise<Blob>;
};

const downloadBlob = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
};

const downloadJSON = (data: unknown, filename: string) => {
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: "application/json",
  });
  downloadBlob(blob, filename);
};

export function DownloadButtons<T>({
  data,
  loading,
  csvFilename,
  jsonFilename,
  onDownloadCsv,
}: DownloadButtonsProps<T>) {
  const [downloadingCsv, setDownloadingCsv] = useState(false);

  const disabled =
    loading || downloadingCsv || !data || data.length === 0;

  return (
    <div className="flex gap-4">
      {/* CSV */}
      <Button
        variant="outline"
        size="sm"
        disabled={disabled}
        onClick={async () => {
          try {
            setDownloadingCsv(true);
            const blob = await onDownloadCsv();
            downloadBlob(blob, csvFilename);
          } finally {
            setDownloadingCsv(false);
          }
        }}
        className="flex items-center gap-2 text-blue-600"
      >
        {downloadingCsv ? (
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
        ) : (
          <Download className="h-4 w-4" />
        )}
        CSV
      </Button>

      {/* JSON */}
      <Button
        variant="outline"
        size="sm"
        disabled={disabled}
        onClick={() => downloadJSON(data, jsonFilename)}
        className="flex items-center gap-2 text-blue-600"
      >
        <Download className="h-4 w-4" />
        JSON
      </Button>
    </div>
  );
}
