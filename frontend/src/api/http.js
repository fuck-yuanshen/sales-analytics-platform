import axios from "axios";

const apiBaseUrl = process.env.VUE_APP_API_BASE_URL || "/api";

const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 20000
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (
      payload &&
      typeof payload === "object" &&
      Object.prototype.hasOwnProperty.call(payload, "success") &&
      payload.success === false
    ) {
      return Promise.reject(new Error(payload.message || "请求失败"));
    }
    return payload;
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || "请求失败";
    return Promise.reject(new Error(message));
  }
);

export default http;

