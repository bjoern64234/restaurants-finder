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