import { Alert } from "@/pages/Alerts/AlertsPage";
import api from "@/services/api";
import { PageableResponse } from "@/types/pagination";

export interface GetAlertsParams {
  page?: number;
  size?: number;
  sort?: string | string[];
}


export async function getAlerts(
  params: GetAlertsParams
): Promise<PageableResponse<Alert>> {
  const queryParams = new URLSearchParams();

  if (params.page !== undefined) {
    queryParams.append("page", params.page.toString());
  }

  if (params.size !== undefined) {
    queryParams.append("size", params.size.toString());
  }

  if (params.sort) {
    if (Array.isArray(params.sort)) {
      params.sort.forEach(sort =>
        queryParams.append("sort", sort)
      );
    } else {
      queryParams.append("sort", params.sort);
    }
  }

  const response = await api.get<PageableResponse<Alert>>(
    `/api/alerts?${queryParams.toString()}`
  );

  return response.data;
}

export async function resolveAlert(
  alertId: number
): Promise<void> {
  await api.patch(`/api/alerts/${alertId}/resolve`);
}
