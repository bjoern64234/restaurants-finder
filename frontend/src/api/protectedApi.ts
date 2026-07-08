import axios from "axios";

export const protectedApi = axios.create({
    baseURL: "/api",
});

protectedApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("sessionToken");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

protectedApi.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            window.dispatchEvent(new Event("auth:sessionExpired"));
        }
        return Promise.reject(error);
    },
);