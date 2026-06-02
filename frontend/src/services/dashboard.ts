import { api, unwrap } from "./http";
import type { DashboardStats } from "./types";

export const dashboardService = {
  getStats: async () => {
    const res = await api.get("/dashboard/stats");
    return unwrap<DashboardStats>(res.data);
  },
};
