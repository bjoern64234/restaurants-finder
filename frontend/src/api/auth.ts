import { publicApi } from "./publicApi";
import { protectedApi } from "./protectedApi.ts";
import type { AxiosResponse } from "axios";
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from "../types/auth.type";

export const login = (data: LoginRequest): Promise<AxiosResponse<AuthResponse>> =>
    publicApi.post("/login", data);

export const register = (data: RegisterRequest): Promise<AxiosResponse<AuthUser>> =>
    publicApi.post("/register", data);

export const logout = (): Promise<AxiosResponse<void>> =>
    protectedApi.post("/logout");