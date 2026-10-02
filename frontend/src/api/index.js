import http from "./http";

const apiBaseUrl = (process.env.VUE_APP_API_BASE_URL || "/api").replace(/\/$/, "");

export const fetchSummary = (payload) => http.post("/analytics/summary", payload);
export const fetchTrend = (payload) => http.post("/analytics/trend", payload);
export const fetchTopProducts = (payload) => http.post("/analytics/top-products", payload);
export const fetchGenderDistribution = (payload) => http.post("/analytics/user-distribution/gender", payload);
export const fetchTagDistribution = (payload) => http.post("/analytics/user-distribution/tag", payload);
export const fetchGeoDistribution = (level, payload) => http.post(`/analytics/geo/${level}`, payload);
export const fetchDrillDown = (payload) => http.post("/analytics/drill-down", payload);

export const uploadOfflineFile = (formData) =>
  http.post("/data-ingestion/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
export const syncExternal = (payload) => http.post("/data-ingestion/sync", payload);
export const triggerScheduledSync = () => http.post("/data-ingestion/sync/scheduled");
export const archiveExpiredData = () => http.post("/data-ingestion/archive");
export const createBackup = (payload) => http.post("/data-ingestion/backup", payload);
export const restoreBackup = (id) => http.post(`/data-ingestion/restore/${id}`);
export const listBackups = () => http.get("/data-ingestion/backups");
export const getRetention = () => http.get("/data-ingestion/retention");
export const setRetention = (payload) => http.put("/data-ingestion/retention", payload);
export const getSyncFrequency = () => http.get("/data-ingestion/sync-frequency");
export const setSyncFrequency = (payload) => http.put("/data-ingestion/sync-frequency", payload);

export const listAlertRules = () => http.get("/alerts/rules");
export const createAlertRule = (payload) => http.post("/alerts/rules", payload);
export const updateAlertRule = (id, payload) => http.put(`/alerts/rules/${id}`, payload);
export const deleteAlertRule = (id) => http.delete(`/alerts/rules/${id}`);
export const listAlertEvents = () => http.get("/alerts/events");
export const evaluateAlerts = () => http.post("/alerts/evaluate");

export const listTemplates = () => http.get("/chart-templates");
export const getTemplate = (id) => http.get(`/chart-templates/${id}`);
export const createTemplate = (payload) => http.post("/chart-templates", payload);
export const updateTemplate = (id, payload) => http.put(`/chart-templates/${id}`, payload);
export const deleteTemplate = (id) => http.delete(`/chart-templates/${id}`);
export const recommendTemplate = (payload) => http.post("/chart-templates/recommend", payload);

export const exportReport = async (payload) => {
  const response = await fetch(`${apiBaseUrl}/reports/export`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "导出失败");
  }
  return response.blob();
};

